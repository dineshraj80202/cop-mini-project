package com.ecommerce.service;

import com.ecommerce.dao.CategoryDao;
import com.ecommerce.dao.impl.CategoryDaoImpl;
import com.ecommerce.model.Category;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing product categories.
 */
public class CategoryService {
    private final CategoryDao categoryDao;

    public CategoryService() {
        this.categoryDao = new CategoryDaoImpl();
    }

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public Category createCategory(String name, String description) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        Optional<Category> existing = categoryDao.findByName(name.trim());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A category with name '" + name + "' already exists.");
        }
        Category category = new Category(name.trim(), description != null ? description.trim() : "");
        return categoryDao.create(category);
    }

    public Optional<Category> getCategoryById(int id) throws SQLException {
        return categoryDao.findById(id);
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDao.findAll();
    }

    public boolean updateCategory(int id, String name, String description) throws SQLException {
        Optional<Category> opt = categoryDao.findById(id);
        if (opt.isEmpty()) return false;

        Category category = opt.get();
        if (name != null && !name.trim().isEmpty()) {
            Optional<Category> duplicate = categoryDao.findByName(name.trim());
            if (duplicate.isPresent() && duplicate.get().getId() != id) {
                throw new IllegalArgumentException("Another category with name '" + name + "' already exists.");
            }
            category.setName(name.trim());
        }
        if (description != null) {
            category.setDescription(description.trim());
        }
        return categoryDao.update(category);
    }

    public boolean deleteCategory(int id) throws SQLException {
        return categoryDao.delete(id);
    }
}
