package com.ecommerce.service;

import com.ecommerce.dao.CartDao;
import com.ecommerce.dao.ProductDao;
import com.ecommerce.dao.impl.CartDaoImpl;
import com.ecommerce.dao.impl.ProductDaoImpl;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service managing customer shopping carts and item line validations.
 */
public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService() {
        this.cartDao = new CartDaoImpl();
        this.productDao = new ProductDaoImpl();
    }

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public Cart getCart(int customerId) throws SQLException {
        return cartDao.findCartByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCustomerId(customerId);
                    return c;
                });
    }

    public boolean addToCart(int customerId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Optional<Product> prodOpt = productDao.findById(productId);
        if (prodOpt.isEmpty() || !prodOpt.get().isActive()) {
            throw new IllegalArgumentException("Product is not available for purchase.");
        }

        Product product = prodOpt.get();
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Requested quantity (" + quantity + 
                    ") exceeds available stock (" + product.getStockQuantity() + ").");
        }

        int cartId = cartDao.getOrCreateCartId(customerId);
        Cart cart = getCart(customerId);

        // Check if item already exists in cart to ensure total doesn't exceed stock
        for (CartItem item : cart.getItems()) {
            if (item.getProductId() == productId) {
                int totalRequested = item.getQuantity() + quantity;
                if (totalRequested > product.getStockQuantity()) {
                    throw new IllegalArgumentException("Cannot add more. You already have " + 
                            item.getQuantity() + " in cart, and total available is " + product.getStockQuantity() + ".");
                }
                break;
            }
        }

        return cartDao.addItem(cartId, productId, quantity);
    }

    public boolean updateQuantity(int customerId, int productId, int newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        int cartId = cartDao.getOrCreateCartId(customerId);
        if (newQuantity == 0) {
            return cartDao.removeItem(cartId, productId);
        }

        Optional<Product> prodOpt = productDao.findById(productId);
        if (prodOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found.");
        }

        Product product = prodOpt.get();
        if (newQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Quantity (" + newQuantity + 
                    ") exceeds available stock (" + product.getStockQuantity() + ").");
        }

        return cartDao.updateItemQuantity(cartId, productId, newQuantity);
    }

    public boolean removeFromCart(int customerId, int productId) throws SQLException {
        int cartId = cartDao.getOrCreateCartId(customerId);
        return cartDao.removeItem(cartId, productId);
    }

    public boolean clearCart(int customerId) throws SQLException {
        int cartId = cartDao.getOrCreateCartId(customerId);
        return cartDao.clearCart(cartId);
    }
}
