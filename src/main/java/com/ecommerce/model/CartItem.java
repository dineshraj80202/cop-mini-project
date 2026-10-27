package com.ecommerce.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain entity representing an individual item line in a Shopping Cart.
 */
public class CartItem {
    private int id;
    private int cartId;
    private int productId;
    private String productSku;
    private String productName;
    private BigDecimal unitPrice;
    private int stockAvailable;
    private int quantity;
    private BigDecimal subtotal;
    private Timestamp addedAt;
    private Timestamp updatedAt;

    public CartItem() {
        this.unitPrice = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.quantity = 1;
    }

    public CartItem(int id, int cartId, int productId, String productSku, String productName, 
                    BigDecimal unitPrice, int stockAvailable, int quantity, 
                    BigDecimal subtotal, Timestamp addedAt, Timestamp updatedAt) {
        this.id = id;
        this.cartId = cartId;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.stockAvailable = stockAvailable;
        this.quantity = quantity;
        this.subtotal = subtotal != null ? subtotal : this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateSubtotal();
    }

    public int getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(int stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void recalculateSubtotal() {
        if (this.unitPrice != null) {
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
