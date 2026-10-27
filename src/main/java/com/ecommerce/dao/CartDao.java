package com.ecommerce.dao;

import com.ecommerce.model.Cart;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Data Access Object interface for Shopping Cart and its line items.
 */
public interface CartDao {
    int getOrCreateCartId(int customerId) throws SQLException;
    Optional<Cart> findCartByCustomerId(int customerId) throws SQLException;
    boolean addItem(int cartId, int productId, int quantity) throws SQLException;
    boolean updateItemQuantity(int cartId, int productId, int quantity) throws SQLException;
    boolean removeItem(int cartId, int productId) throws SQLException;
    boolean clearCart(int cartId) throws SQLException;
    boolean clearCart(Connection conn, int cartId) throws SQLException; // For transactions
}
