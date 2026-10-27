package com.ecommerce.model;

/**
 * Represents the role of a user within the E-Commerce system.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER;

    public static UserRole fromString(String roleStr) {
        if (roleStr == null) return CUSTOMER;
        try {
            return UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER;
        }
    }
}
