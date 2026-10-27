package com.ecommerce.ui;

import com.ecommerce.model.User;

/**
 * Stores the session context for the currently authenticated user.
 */
public class UserSession {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static void clear() {
        currentUser = null;
    }
}
