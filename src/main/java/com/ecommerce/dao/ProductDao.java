package com.ecommerce.dao;

import com.ecommerce.model.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product Catalog and Inventory.
 */
public interface ProductDao {
    Product create(Product product) throws SQLException;
    Optional<Product> findById(int id) throws SQLException;
    Optional<Product> findBySku(String sku) throws SQLException;
    List<Product> findAll(boolean includeInactive) throws SQLException;
    List<Product> findByCategory(int categoryId) throws SQLException;
    List<Product> search(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) throws SQLException;
    List<Product> findLowStock(int threshold) throws SQLException;
    boolean update(Product product) throws SQLException;
    boolean updateStock(Connection conn, int productId, int quantityDelta) throws SQLException;
    boolean delete(int id) throws SQLException;
    boolean toggleStatus(int id, boolean active) throws SQLException;
}
