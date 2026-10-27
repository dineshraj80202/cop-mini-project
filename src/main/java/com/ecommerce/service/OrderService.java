package com.ecommerce.service;

import com.ecommerce.dao.CartDao;
import com.ecommerce.dao.OrderDao;
import com.ecommerce.dao.impl.CartDaoImpl;
import com.ecommerce.dao.impl.OrderDaoImpl;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing orders, checkout orchestration, status progression, and cancellation.
 */
public class OrderService {
    private final OrderDao orderDao;
    private final CartDao cartDao;

    public OrderService() {
        this.orderDao = new OrderDaoImpl();
        this.cartDao = new CartDaoImpl();
    }

    public OrderService(OrderDao orderDao, CartDao cartDao) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
    }

    /**
     * Executes checkout: takes current cart, converts to Order and OrderItems,
     * deducts stock and clears cart inside an ACID database transaction.
     */
    public Order checkout(int customerId, String shippingAddress, String paymentMethod) throws SQLException {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("A valid shipping address is required for checkout.");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CREDIT_CARD";
        }

        Optional<Cart> cartOpt = cartDao.findCartByCustomerId(customerId);
        if (cartOpt.isEmpty() || cartOpt.get().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty shopping cart.");
        }

        Cart cart = cartOpt.get();

        // Generate Order
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "ORD-" + timestamp + "-" + shortId;

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(customerId);
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress.trim());
        order.setPaymentMethod(paymentMethod.trim().toUpperCase());

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem(
                    ci.getProductId(),
                    ci.getProductName(),
                    ci.getProductSku(),
                    ci.getUnitPrice(),
                    ci.getQuantity()
            );
            order.addItem(oi);
        }

        return orderDao.createOrderWithItems(order, cart.getId());
    }

    public List<Order> getCustomerOrders(int customerId) throws SQLException {
        return orderDao.findByCustomerId(customerId);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDao.findAll();
    }

    public Optional<Order> getOrderById(int orderId) throws SQLException {
        return orderDao.findById(orderId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) throws SQLException {
        return orderDao.findByOrderNumber(orderNumber);
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) throws SQLException {
        Optional<Order> orderOpt = orderDao.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order ID " + orderId + " does not exist.");
        }
        Order order = orderOpt.get();
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of a cancelled order.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED && status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot transition back to another status.");
        }

        return orderDao.updateStatus(orderId, status);
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        return orderDao.cancelOrder(orderId);
    }
}
