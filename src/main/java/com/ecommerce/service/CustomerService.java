package com.ecommerce.service;

import com.ecommerce.dao.UserDao;
import com.ecommerce.dao.impl.UserDaoImpl;
import com.ecommerce.model.User;
import com.ecommerce.model.UserRole;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing customer profile and customer directory operations.
 */
public class CustomerService {
    private final UserDao userDao;

    public CustomerService() {
        this.userDao = new UserDaoImpl();
    }

    public CustomerService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> getCustomerById(int customerId) throws SQLException {
        return userDao.findById(customerId);
    }

    public boolean updateProfile(int customerId, String fullName, String phone, String address) throws SQLException {
        Optional<User> userOpt = userDao.findById(customerId);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (fullName != null && !fullName.trim().isEmpty()) user.setFullName(fullName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (address != null) user.setAddress(address.trim());

        return userDao.update(user);
    }

    public List<User> getAllCustomers() throws SQLException {
        return userDao.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() throws SQLException {
        return userDao.findAll();
    }
}
