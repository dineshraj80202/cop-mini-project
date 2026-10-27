package com.ecommerce.service;

import com.ecommerce.dao.UserDao;
import com.ecommerce.dao.impl.UserDaoImpl;
import com.ecommerce.model.User;
import com.ecommerce.model.UserRole;
import com.ecommerce.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service handling user authentication and registration workflows.
 */
public class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDaoImpl();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user with username and plain password.
     */
    public Optional<User> login(String username, String password) throws SQLException {
        if (username == null || password == null) return Optional.empty();

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            // Also try email login
            userOpt = userDao.findByEmail(username.trim());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (valid) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a new customer account.
     */
    public User register(String username, String password, String fullName, String email, 
                         String phone, String address) throws SQLException, IllegalArgumentException {
        // Validation
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }

        if (userDao.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userDao.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(hash);
        user.setSalt(salt);
        user.setFullName(fullName != null ? fullName.trim() : username);
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        user.setAddress(address != null ? address.trim() : "");
        user.setRole(UserRole.CUSTOMER);

        return userDao.create(user);
    }

    /**
     * Changes user password verifying the current password first.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(oldPassword, user.getSalt(), user.getPasswordHash())) {
            return false;
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        return userDao.updatePassword(userId, newHash, newSalt);
    }
}
