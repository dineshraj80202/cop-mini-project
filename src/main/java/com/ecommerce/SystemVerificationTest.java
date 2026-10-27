package com.ecommerce;

import java.math.BigDecimal;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.util.AnsiColor;
import com.ecommerce.util.ConsoleTable;
import com.ecommerce.util.DatabaseConnection;
import com.ecommerce.util.PasswordUtil;

/**
 * Self-contained offline verification test for the E-Commerce architecture,
 * models, security hashing, table formatter, and configuration integrity.
 */
public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println(AnsiColor.bold("=================================================="));
        System.out.println(AnsiColor.color("   E-COMMERCE SYSTEM ARCHITECTURE VERIFICATION", AnsiColor.BOLD_CYAN));
        System.out.println(AnsiColor.bold("=================================================="));

        testPasswordSecurity();
        testModelsAndCalculations();
        testConsoleTableRendering();
        testDatabaseConfiguration();

        System.out.println(AnsiColor.bold("\n=================================================="));
        System.out.println(AnsiColor.success("   ALL SYSTEM VERIFICATION CHECKS PASSED [OK]"));
        System.out.println(AnsiColor.bold("=================================================="));
    }

    private static void testPasswordSecurity() {
        System.out.print("Testing PasswordUtil (Salt + SHA-256)... ");
        String salt = PasswordUtil.generateSalt();
        String password = "SecretPassword123!";
        String hash = PasswordUtil.hashPassword(password, salt);

        boolean valid = PasswordUtil.verifyPassword(password, salt, hash);
        boolean invalid = PasswordUtil.verifyPassword("WrongPassword", salt, hash);

        if (valid && !invalid && hash.length() == 64) {
            System.out.println(AnsiColor.success("[PASSED]"));
        } else {
            throw new RuntimeException("Password security check failed!");
        }
    }

    private static void testModelsAndCalculations() {
        System.out.print("Testing Domain Models & Cart/Order Calculations... ");

        Product prod1 = new Product(1, 1, "Electronics", "SKU-001", "MacBook Pro", "M3 Laptop", new BigDecimal("1999.99"), 10, true, null, null);
        Product prod2 = new Product(2, 1, "Electronics", "SKU-002", "Magic Mouse", "Mouse", new BigDecimal("99.50"), 20, true, null, null);

        Cart cart = new Cart();
        cart.setId(10);
        cart.setCustomerId(100);

        CartItem item1 = new CartItem(1, 10, prod1.getId(), prod1.getSku(), prod1.getName(), prod1.getPrice(), prod1.getStockQuantity(), 2, null, null, null);
        CartItem item2 = new CartItem(2, 10, prod2.getId(), prod2.getSku(), prod2.getName(), prod2.getPrice(), prod2.getStockQuantity(), 1, null, null, null);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        BigDecimal expectedTotal = new BigDecimal("1999.99").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("99.50"));

        if (cart.getTotalAmount().compareTo(expectedTotal) != 0) {
            throw new RuntimeException("Cart calculation mismatch! Expected: " + expectedTotal + ", Got: " + cart.getTotalAmount());
        }

        if (cart.getTotalItemCount() != 3) {
            throw new RuntimeException("Item count mismatch! Expected 3, got: " + cart.getTotalItemCount());
        }

        Order order = new Order();
        order.setOrderNumber("ORD-TEST-12345");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(cart.getTotalAmount());

        for (CartItem ci : cart.getItems()) {
            order.addItem(new OrderItem(ci.getProductId(), ci.getProductName(), ci.getProductSku(), ci.getUnitPrice(), ci.getQuantity()));
        }

        if (order.getItems().size() != 2) {
            throw new RuntimeException("Order item count mismatch!");
        }

        System.out.println(AnsiColor.success("[PASSED]"));
    }

    private static void testConsoleTableRendering() {
        System.out.println("\nTesting ConsoleTable Rendering Format:");
        ConsoleTable table = new ConsoleTable("ID", "SKU", "Product Name", "Unit Price", "Qty", "Subtotal");
        table.addRow("1", "SKU-001", "MacBook Pro 14\"", "$1999.99", "2", "$3999.98");
        table.addRow("2", "SKU-002", "Magic Mouse", "$99.50", "1", "$99.50");
        table.print();
        System.out.println(AnsiColor.success("Table formatted successfully."));
    }

    private static void testDatabaseConfiguration() {
        System.out.print("Testing Database Configuration Reader... ");
        String url = DatabaseConnection.getUrl();
        String user = DatabaseConnection.getUser();
        if (url != null && user != null && url.contains("mysql")) {
            System.out.println(AnsiColor.success("[PASSED] (Target: " + user + "@" + url + ")"));
        } else {
            throw new RuntimeException("Configuration loading check failed!");
        }
    }
}
