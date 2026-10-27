package com.ecommerce.dao.impl;

import com.ecommerce.dao.OrderDao;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
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
 * JDBC implementation of OrderDao with full ACID Transaction management.
 */
public class OrderDaoImpl implements OrderDao {

    @Override
    public Order createOrderWithItems(Order order, int cartIdToClear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 1. Verify and deduct stock atomically with row-level locks
            String stockLockSql = "SELECT name, stock_quantity, is_active FROM products WHERE id = ? FOR UPDATE";
            String stockDeductSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";

            try (PreparedStatement lockPs = conn.prepareStatement(stockLockSql);
                 PreparedStatement deductPs = conn.prepareStatement(stockDeductSql)) {

                for (OrderItem item : order.getItems()) {
                    if (item.getProductId() == null) {
                        throw new SQLException("Cannot order item without a valid product ID");
                    }
                    lockPs.setInt(1, item.getProductId());
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product not found: " + item.getProductName());
                        }
                        boolean active = rs.getBoolean("is_active");
                        int stock = rs.getInt("stock_quantity");

                        if (!active) {
                            throw new SQLException("Product is no longer available: " + rs.getString("name"));
                        }
                        if (stock < item.getQuantity()) {
                            throw new SQLException("Insufficient stock for '" + rs.getString("name") +
                                    "'. Requested: " + item.getQuantity() + ", Available: " + stock);
                        }
                    }

                    // Deduct stock
                    deductPs.setInt(1, item.getQuantity());
                    deductPs.setInt(2, item.getProductId());
                    deductPs.executeUpdate();
                }
            }

            // 2. Insert into orders table
            String orderSql = "INSERT INTO orders (order_number, customer_id, total_amount, status, shipping_address, payment_method) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderPs.setString(1, order.getOrderNumber());
                orderPs.setInt(2, order.getCustomerId());
                orderPs.setBigDecimal(3, order.getTotalAmount());
                orderPs.setString(4, order.getStatus().name());
                orderPs.setString(5, order.getShippingAddress());
                orderPs.setString(6, order.getPaymentMethod());

                orderPs.executeUpdate();
                try (ResultSet keys = orderPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setId(keys.getInt(1));
                    } else {
                        throw new SQLException("Failed to obtain generated ID for order.");
                    }
                }
            }

            // 3. Insert order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, product_sku, unit_price, quantity, subtotal) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement itemPs = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                for (OrderItem item : order.getItems()) {
                    itemPs.setInt(1, order.getId());
                    if (item.getProductId() != null) {
                        itemPs.setInt(2, item.getProductId());
                    } else {
                        itemPs.setNull(2, java.sql.Types.INTEGER);
                    }
                    itemPs.setString(3, item.getProductName());
                    itemPs.setString(4, item.getProductSku());
                    itemPs.setBigDecimal(5, item.getUnitPrice());
                    itemPs.setInt(6, item.getQuantity());
                    itemPs.setBigDecimal(7, item.getSubtotal());

                    itemPs.addBatch();
                }
                itemPs.executeBatch();
            }

            // 4. Clear shopping cart if requested
            if (cartIdToClear > 0) {
                String clearCartSql = "DELETE FROM cart_items WHERE cart_id = ?";
                try (PreparedStatement clearPs = conn.prepareStatement(clearCartSql)) {
                    clearPs.setInt(1, cartIdToClear);
                    clearPs.executeUpdate();
                }
            }

            // Commit Transaction
            conn.commit();
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public Optional<Order> findById(int id) throws SQLException {
        String sql = "SELECT o.*, u.full_name AS customer_name, u.email AS customer_email " +
                     "FROM orders o JOIN users u ON o.customer_id = u.id WHERE o.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(loadOrderItems(conn, order.getId()));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) throws SQLException {
        String sql = "SELECT o.*, u.full_name AS customer_name, u.email AS customer_email " +
                     "FROM orders o JOIN users u ON o.customer_id = u.id WHERE o.order_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(loadOrderItems(conn, order.getId()));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name AS customer_name, u.email AS customer_email " +
                     "FROM orders o JOIN users u ON o.customer_id = u.id " +
                     "WHERE o.customer_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(loadOrderItems(conn, order.getId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name AS customer_name, u.email AS customer_email " +
                     "FROM orders o JOIN users u ON o.customer_id = u.id ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(loadOrderItems(conn, order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public boolean updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cancelOrder(int orderId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // Check current order status
            String checkSql = "SELECT status FROM orders WHERE id = ? FOR UPDATE";
            OrderStatus currentStatus;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, orderId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Order ID not found: " + orderId);
                    }
                    currentStatus = OrderStatus.fromString(rs.getString("status"));
                }
            }

            if (currentStatus == OrderStatus.CANCELLED) {
                throw new SQLException("Order is already cancelled.");
            }
            if (currentStatus == OrderStatus.DELIVERED) {
                throw new SQLException("Delivered orders cannot be cancelled.");
            }

            // Restore stock for all items in this order
            List<OrderItem> items = loadOrderItems(conn, orderId);
            String restoreStockSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
            try (PreparedStatement restorePs = conn.prepareStatement(restoreStockSql)) {
                for (OrderItem item : items) {
                    if (item.getProductId() != null) {
                        restorePs.setInt(1, item.getQuantity());
                        restorePs.setInt(2, item.getProductId());
                        restorePs.addBatch();
                    }
                }
                restorePs.executeBatch();
            }

            // Update order status to CANCELLED
            String updateSql = "UPDATE orders SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setInt(1, orderId);
                updatePs.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private List<OrderItem> loadOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer prodId = rs.getInt("product_id");
                    if (rs.wasNull()) prodId = null;

                    OrderItem item = new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            prodId,
                            rs.getString("product_name"),
                            rs.getString("product_sku"),
                            rs.getBigDecimal("unit_price"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("subtotal")
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("id"),
                rs.getString("order_number"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getBigDecimal("total_amount"),
                OrderStatus.fromString(rs.getString("status")),
                rs.getString("shipping_address"),
                rs.getString("payment_method"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                new ArrayList<>()
        );
    }
}
