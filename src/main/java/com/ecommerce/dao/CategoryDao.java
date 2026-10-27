package com.ecommerce.dao;

import com.ecommerce.model.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product Categories.
 */
public interface CategoryDao {
    Category create(Category category) throws SQLException;
    Optional<Category> findById(int id) throws SQLException;
    Optional<Category> findByName(String name) throws SQLException;
    List<Category> findAll() throws SQLException;
    boolean update(Category category) throws SQLException;
    boolean delete(int id) throws SQLException;
}
