package com.ecommerce.dao.impl;

import com.ecommerce.dao.CartDao;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CartDao.
 */
public class CartDaoImpl implements CartDao {

    @Override
    public int getOrCreateCartId(int customerId) throws SQLException {
        String findSql = "SELECT id FROM carts WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Create new cart
        String insertSql = "INSERT INTO carts (customer_id) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create or retrieve cart for customer ID: " + customerId);
    }

    @Override
    public Optional<Cart> findCartByCustomerId(int customerId) throws SQLException {
        int cartId = getOrCreateCartId(customerId);

        String sql = "SELECT ci.id AS item_id, ci.cart_id, ci.product_id, ci.quantity, ci.added_at, ci.updated_at, " +
                     "p.sku, p.name AS product_name, p.price, p.stock_quantity AS stock_available, p.is_active " +
                     "FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.cart_id = ? ORDER BY ci.added_at ASC";

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCustomerId(customerId);
        List<CartItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    BigDecimal unitPrice = rs.getBigDecimal("price");
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

                    CartItem item = new CartItem(
                            rs.getInt("item_id"),
                            rs.getInt("cart_id"),
                            rs.getInt("product_id"),
                            rs.getString("sku"),
                            rs.getString("product_name"),
                            unitPrice,
                            rs.getInt("stock_available"),
                            quantity,
                            subtotal,
                            rs.getTimestamp("added_at"),
                            rs.getTimestamp("updated_at")
                    );
                    items.add(item);
                }
            }
        }

        cart.setItems(items);
        return Optional.of(cart);
    }

    @Override
    public boolean addItem(int cartId, int productId, int quantity) throws SQLException {
        // Upsert cart item: if exists, increment quantity
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateItemQuantity(int cartId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeItem(cartId, productId);
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeItem(int cartId, int productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clearCart(int cartId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return clearCart(conn, cartId);
        }
    }

    @Override
    public boolean clearCart(Connection conn, int cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
            return true;
        }
    }
}
