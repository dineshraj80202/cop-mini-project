package com.ecommerce.dao;

import com.ecommerce.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Users / Customers.
 */
public interface UserDao {
    User create(User user) throws SQLException;
    Optional<User> findById(int id) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findByEmail(String email) throws SQLException;
    boolean update(User user) throws SQLException;
    boolean updatePassword(int userId, String passwordHash, String salt) throws SQLException;
    boolean delete(int id) throws SQLException;
    List<User> findAll() throws SQLException;
}
