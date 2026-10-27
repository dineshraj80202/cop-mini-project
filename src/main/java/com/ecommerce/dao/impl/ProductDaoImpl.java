package com.ecommerce.dao.impl;

import com.ecommerce.dao.ProductDao;
import com.ecommerce.model.Apparel;
import com.ecommerce.model.Product;
import com.ecommerce.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ProductDao.
 */
public class ProductDaoImpl implements ProductDao {

    @Override
    public Product create(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, sku, name, description, price, stock_quantity, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getPrice());
            ps.setInt(6, product.getStockQuantity());
            ps.setBoolean(7, product.isActive());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }
            return product;
        }
    }

    @Override
    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findBySku(String sku) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id WHERE p.sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll(boolean includeInactive) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id ";
        if (!includeInactive) {
            sql += "WHERE p.is_active = TRUE ";
        }
        sql += "ORDER BY p.name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> findByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.category_id = ? AND p.is_active = TRUE ORDER BY p.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public List<Product> search(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name AS category_name FROM products p " +
                "JOIN categories c ON p.category_id = c.id WHERE p.is_active = TRUE "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ? OR p.sku LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }
        sql.append("ORDER BY p.price ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public List<Product> findLowStock(int threshold) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.stock_quantity <= ? AND p.is_active = TRUE ORDER BY p.stock_quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, sku = ?, name = ?, description = ?, " +
                     "price = ?, stock_quantity = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getPrice());
            ps.setInt(6, product.getStockQuantity());
            ps.setBoolean(7, product.isActive());
            ps.setInt(8, product.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStock(Connection conn, int productId, int quantityDelta) throws SQLException {
        // Safe atomic stock update protecting against negative stock
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? " +
                     "WHERE id = ? AND (stock_quantity + ?) >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityDelta);
            ps.setInt(2, productId);
            ps.setInt(3, quantityDelta);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean toggleStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE products SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int categoryId = rs.getInt("category_id");
        String categoryName = rs.getString("category_name");
        String sku = rs.getString("sku");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal price = rs.getBigDecimal("price");
        int stockQuantity = rs.getInt("stock_quantity");
        boolean active = rs.getBoolean("is_active");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        // Object-Oriented Polymorphism: Instantiate Apparel model for fashion items
        if (categoryId == 3 || (categoryName != null && (categoryName.equalsIgnoreCase("Clothes") || categoryName.equalsIgnoreCase("Apparel")))) {
            String size = (sku != null && sku.contains("DNM")) ? "32x32" : "L";
            String brand = (name != null && name.contains("Levi")) ? "Levi's" :
                          ((name != null && name.contains("Hilfiger")) ? "Tommy Hilfiger" : "Flipkart Fashion");
            String material = (description != null && description.toLowerCase().contains("denim")) ? "Stretch Denim" : "Polyester / Cotton";
            String gender = "Men";
            String color = (description != null && description.toLowerCase().contains("jeans")) ? "Indigo Dark Wash" : "Midnight Navy";
            return new Apparel(id, categoryId, sku, name, description, price, stockQuantity, size, brand, material, gender, color);
        }

        return new Product(
                id,
                categoryId,
                categoryName,
                sku,
                name,
                description,
                price,
                stockQuantity,
                active,
                createdAt,
                updatedAt
        );
    }
}
