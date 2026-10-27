package com.ecommerce.service;

import com.ecommerce.dao.CategoryDao;
import com.ecommerce.dao.ProductDao;
import com.ecommerce.dao.impl.CategoryDaoImpl;
import com.ecommerce.dao.impl.ProductDaoImpl;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing product catalog, inventory adjustments, and catalog searching.
 */
public class ProductService {
    private final ProductDao productDao;
    private final CategoryDao categoryDao;

    public ProductService() {
        this.productDao = new ProductDaoImpl();
        this.categoryDao = new CategoryDaoImpl();
    }

    public ProductService(ProductDao productDao, CategoryDao categoryDao) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
    }

    public Product addProduct(int categoryId, String sku, String name, String description, 
                             BigDecimal price, int stockQuantity) throws SQLException {
        // Validation
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be non-negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Initial stock quantity cannot be negative.");
        }
        if (categoryDao.findById(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Selected category ID (" + categoryId + ") does not exist.");
        }
        if (productDao.findBySku(sku.trim()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU '" + sku + "' already exists.");
        }

        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setSku(sku.trim().toUpperCase());
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : "");
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setActive(true);

        return productDao.create(product);
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product.getId() <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return productDao.update(product);
    }

    public boolean adjustStock(int productId, int quantityDelta) throws SQLException {
        Optional<Product> prodOpt = productDao.findById(productId);
        if (prodOpt.isEmpty()) {
            throw new IllegalArgumentException("Product ID " + productId + " does not exist.");
        }
        Product prod = prodOpt.get();
        int newStock = prod.getStockQuantity() + quantityDelta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero. Current stock: " + prod.getStockQuantity());
        }
        prod.setStockQuantity(newStock);
        return productDao.update(prod);
    }

    public boolean setStock(int productId, int newStock) throws SQLException {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        Optional<Product> prodOpt = productDao.findById(productId);
        if (prodOpt.isEmpty()) {
            throw new IllegalArgumentException("Product ID " + productId + " does not exist.");
        }
        Product prod = prodOpt.get();
        prod.setStockQuantity(newStock);
        return productDao.update(prod);
    }

    public Optional<Product> getProductById(int id) throws SQLException {
        return productDao.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) throws SQLException {
        return productDao.findBySku(sku);
    }

    public List<Product> getAllProducts(boolean includeInactive) throws SQLException {
        return productDao.findAll(includeInactive);
    }

    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDao.findByCategory(categoryId);
    }

    public List<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        return productDao.search(keyword, categoryId, minPrice, maxPrice);
    }

    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        return productDao.findLowStock(threshold);
    }

    public boolean toggleProductStatus(int productId, boolean active) throws SQLException {
        return productDao.toggleStatus(productId, active);
    }

    public boolean deleteProduct(int productId) throws SQLException {
        return productDao.delete(productId);
    }
}
