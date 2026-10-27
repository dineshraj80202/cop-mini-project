package com.ecommerce.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain entity representing Apparel in Flipkart's product catalog.
 * Extends base Product model with fashion-specific attributes (size, brand, material, gender, color).
 * Categorized strictly as 'Apparel' (not generic 'Clothes').
 */
public class Apparel extends Product {
    private String size;       // e.g., "S", "M", "L", "XL", "32", "34"
    private String brand;      // e.g., "Levi's", "Tommy Hilfiger", "Puma", "Nike"
    private String material;   // e.g., "Denim", "100% Cotton", "Polyester Blend"
    private String gender;     // e.g., "Men", "Women", "Unisex"
    private String color;      // e.g., "Indigo Blue", "Navy", "Jet Black"

    public Apparel() {
        super();
        setCategoryName("Clothes");
    }

    public Apparel(int id, int categoryId, String sku, String name, 
                   String description, BigDecimal price, int stockQuantity,
                   String size, String brand, String material, String gender, String color) {
        super(id, categoryId, "Clothes", sku, name, description, price, stockQuantity, true, 
              new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        this.size = size;
        this.brand = brand;
        this.material = material;
        this.gender = gender;
        this.color = color;
    }

    public Apparel(Product product, String size, String brand, String material, String gender, String color) {
        super(product.getId(), product.getCategoryId(), "Clothes", product.getSku(), 
              product.getName(), product.getDescription(), product.getPrice(), 
              product.getStockQuantity(), product.isActive(), product.getCreatedAt(), product.getUpdatedAt());
        this.size = size;
        this.brand = brand;
        this.material = material;
        this.gender = gender;
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Apparel{" +
                "id=" + getId() +
                ", sku='" + getSku() + '\'' +
                ", name='" + getName() + '\'' +
                ", brand='" + brand + '\'' +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", material='" + material + '\'' +
                ", gender='" + gender + '\'' +
                ", price=" + getPrice() +
                ", stock=" + getStockQuantity() +
                ", category='Apparel'" +
                '}';
    }
}
