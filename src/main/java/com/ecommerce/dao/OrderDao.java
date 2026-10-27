package com.ecommerce.dao;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Orders and Order Items with ACID transaction support.
 */
public interface OrderDao {
    Order createOrderWithItems(Order order, int cartIdToClear) throws SQLException;
    Optional<Order> findById(int id) throws SQLException;
    Optional<Order> findByOrderNumber(String orderNumber) throws SQLException;
    List<Order> findByCustomerId(int customerId) throws SQLException;
    List<Order> findAll() throws SQLException;
    boolean updateStatus(int orderId, OrderStatus status) throws SQLException;
    boolean cancelOrder(int orderId) throws SQLException; // Cancels and restores stock in transaction
}
