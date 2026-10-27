package com.ecommerce.ui;

import com.ecommerce.model.Category;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.AnsiColor;
import com.ecommerce.util.ConsoleTable;
import com.ecommerce.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Console UI controller for the Administrator portal.
 */
public class AdminMenu {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final CustomerService customerService;

    public AdminMenu() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.orderService = new OrderService();
        this.customerService = new CustomerService();
    }

    public void display() {
        while (UserSession.isAdmin()) {
            System.out.println("\n" + AnsiColor.bold("=================================================="));
            System.out.println(AnsiColor.color("        ADMINISTRATION CONTROL PANEL", AnsiColor.BOLD_PURPLE));
            System.out.println(AnsiColor.bold("=================================================="));
            System.out.println(" Logged in as: " + AnsiColor.bold(UserSession.getCurrentUser().getFullName()));
            System.out.println(" 1. Product Catalog Management (CRUD)");
            System.out.println(" 2. Category Management (CRUD)");
            System.out.println(" 3. Inventory & Low Stock Alerts");
            System.out.println(" 4. Order Management & Fulfillment");
            System.out.println(" 5. Customer Directory & Insights");
            System.out.println(" 6. Logout to Main Menu");
            System.out.println("==================================================");

            int choice = InputValidator.readInt("Select Option", 1, 6);
            switch (choice) {
                case 1 -> handleProductManagement();
                case 2 -> handleCategoryManagement();
                case 3 -> handleInventoryManagement();
                case 4 -> handleOrderManagement();
                case 5 -> handleCustomerManagement();
                case 6 -> {
                    UserSession.clear();
                    System.out.println(AnsiColor.success("Logged out successfully."));
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Product Management
    // -------------------------------------------------------------
    private void handleProductManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + AnsiColor.info("--- [ADMIN] Product Catalog Management ---"));
            System.out.println("1. List All Products");
            System.out.println("2. Add New Product");
            System.out.println("3. Edit Existing Product");
            System.out.println("4. Toggle Product Status (Active/Inactive)");
            System.out.println("5. Delete Product");
            System.out.println("6. Back to Admin Menu");

            int option = InputValidator.readInt("Select", 1, 6);
            switch (option) {
                case 1 -> listAllProducts(true);
                case 2 -> addNewProduct();
                case 3 -> editProduct();
                case 4 -> toggleProductStatus();
                case 5 -> deleteProduct();
                case 6 -> back = true;
            }
        }
    }

    private void listAllProducts(boolean includeInactive) {
        try {
            List<Product> products = productService.getAllProducts(includeInactive);
            ConsoleTable table = new ConsoleTable("ID", "SKU", "Name", "Category", "Price", "Stock", "Status");
            for (Product p : products) {
                table.addRow(
                        String.valueOf(p.getId()),
                        p.getSku(),
                        p.getName(),
                        p.getCategoryName() != null ? p.getCategoryName() : "N/A",
                        "$" + p.getPrice().toPlainString(),
                        String.valueOf(p.getStockQuantity()),
                        p.isActive() ? AnsiColor.color("Active", AnsiColor.GREEN) : AnsiColor.color("Inactive", AnsiColor.RED)
                );
            }
            System.out.println("\n" + AnsiColor.bold("Product Inventory List:"));
            table.print();
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Error retrieving products: " + e.getMessage()));
        }
    }

    private void addNewProduct() {
        System.out.println("\n" + AnsiColor.info("--- Add New Product ---"));
        try {
            List<Category> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println(AnsiColor.warning("No categories exist. Please create a category first!"));
                return;
            }

            ConsoleTable catTable = new ConsoleTable("ID", "Category Name");
            for (Category c : categories) {
                catTable.addRow(String.valueOf(c.getId()), c.getName());
            }
            catTable.print();

            int categoryId = InputValidator.readInt("Enter Category ID", 1, Integer.MAX_VALUE);
            String sku = InputValidator.readString("Enter Product SKU (e.g. ELEC-001)", false);
            String name = InputValidator.readString("Enter Product Name", false);
            String description = InputValidator.readString("Enter Description", true);
            BigDecimal price = InputValidator.readBigDecimal("Enter Price (USD)", BigDecimal.ZERO, new BigDecimal("1000000.00"));
            int stock = InputValidator.readInt("Enter Initial Stock Quantity", 0, 100000);

            Product created = productService.addProduct(categoryId, sku, name, description, price, stock);
            System.out.println(AnsiColor.success("Product created successfully! Assigned ID: " + created.getId()));
        } catch (IllegalArgumentException e) {
            System.out.println(AnsiColor.error("Validation error: " + e.getMessage()));
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database error: " + e.getMessage()));
        }
    }

    private void editProduct() {
        listAllProducts(true);
        int id = InputValidator.readInt("Enter Product ID to edit", 1, Integer.MAX_VALUE);
        try {
            Optional<Product> prodOpt = productService.getProductById(id);
            if (prodOpt.isEmpty()) {
                System.out.println(AnsiColor.error("Product not found with ID: " + id));
                return;
            }

            Product product = prodOpt.get();
            System.out.println("Editing product: " + AnsiColor.bold(product.getName()));
            System.out.println("(Press Enter without typing to keep current value)");

            String newName = InputValidator.readString("New Name [" + product.getName() + "]", true);
            if (!newName.isEmpty()) product.setName(newName);

            String newDesc = InputValidator.readString("New Description [" + product.getDescription() + "]", true);
            if (!newDesc.isEmpty()) product.setDescription(newDesc);

            String priceStr = InputValidator.readString("New Price [" + product.getPrice() + "]", true);
            if (!priceStr.isEmpty()) {
                try {
                    product.setPrice(new BigDecimal(priceStr));
                } catch (Exception e) {
                    System.out.println(AnsiColor.warning("Invalid price; keeping previous price."));
                }
            }

            String stockStr = InputValidator.readString("New Stock [" + product.getStockQuantity() + "]", true);
            if (!stockStr.isEmpty()) {
                try {
                    product.setStockQuantity(Integer.parseInt(stockStr));
                } catch (Exception e) {
                    System.out.println(AnsiColor.warning("Invalid stock; keeping previous stock."));
                }
            }

            boolean ok = productService.updateProduct(product);
            if (ok) {
                System.out.println(AnsiColor.success("Product updated successfully!"));
            } else {
                System.out.println(AnsiColor.error("Failed to update product."));
            }
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database error: " + e.getMessage()));
        }
    }

    private void toggleProductStatus() {
        listAllProducts(true);
        int id = InputValidator.readInt("Enter Product ID to toggle status", 1, Integer.MAX_VALUE);
        try {
            Optional<Product> prodOpt = productService.getProductById(id);
            if (prodOpt.isEmpty()) {
                System.out.println(AnsiColor.error("Product not found."));
                return;
            }
            Product p = prodOpt.get();
            boolean newStatus = !p.isActive();
            productService.toggleProductStatus(id, newStatus);
            System.out.println(AnsiColor.success("Product status changed to: " + (newStatus ? "Active" : "Inactive")));
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database error: " + e.getMessage()));
        }
    }

    private void deleteProduct() {
        listAllProducts(true);
        int id = InputValidator.readInt("Enter Product ID to permanently delete", 1, Integer.MAX_VALUE);
        boolean confirm = InputValidator.readConfirmation("Are you sure you want to delete product #" + id + "?");
        if (!confirm) return;

        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                System.out.println(AnsiColor.success("Product deleted successfully."));
            } else {
                System.out.println(AnsiColor.error("Could not delete product (it may be linked to previous orders). Consider deactivating it instead."));
            }
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Cannot delete product: " + e.getMessage() + ". Tip: Deactivate it instead to preserve order histories."));
        }
    }

    // -------------------------------------------------------------
    // Category Management
    // -------------------------------------------------------------
    private void handleCategoryManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + AnsiColor.info("--- [ADMIN] Category Management ---"));
            System.out.println("1. List All Categories");
            System.out.println("2. Add New Category");
            System.out.println("3. Edit Category");
            System.out.println("4. Delete Category");
            System.out.println("5. Back to Admin Menu");

            int option = InputValidator.readInt("Select", 1, 5);
            switch (option) {
                case 1 -> listCategories();
                case 2 -> addCategory();
                case 3 -> editCategory();
                case 4 -> deleteCategory();
                case 5 -> back = true;
            }
        }
    }

    private void listCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            ConsoleTable table = new ConsoleTable("ID", "Category Name", "Description");
            for (Category c : categories) {
                table.addRow(String.valueOf(c.getId()), c.getName(), c.getDescription());
            }
            table.print();
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Error retrieving categories: " + e.getMessage()));
        }
    }

    private void addCategory() {
        System.out.println("\n" + AnsiColor.info("--- Add New Category ---"));
        String name = InputValidator.readString("Category Name", false);
        String desc = InputValidator.readString("Description", true);
        try {
            Category cat = categoryService.createCategory(name, desc);
            System.out.println(AnsiColor.success("Category created with ID: " + cat.getId()));
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Error creating category: " + e.getMessage()));
        }
    }

    private void editCategory() {
        listCategories();
        int id = InputValidator.readInt("Enter Category ID to edit", 1, Integer.MAX_VALUE);
        String newName = InputValidator.readString("New Name (leave blank to keep current)", true);
        String newDesc = InputValidator.readString("New Description (leave blank to keep current)", true);
        try {
            boolean ok = categoryService.updateCategory(id, newName.isEmpty() ? null : newName, newDesc.isEmpty() ? null : newDesc);
            if (ok) System.out.println(AnsiColor.success("Category updated!"));
            else System.out.println(AnsiColor.error("Category not found."));
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Error updating category: " + e.getMessage()));
        }
    }

    private void deleteCategory() {
        listCategories();
        int id = InputValidator.readInt("Enter Category ID to delete", 1, Integer.MAX_VALUE);
        boolean confirm = InputValidator.readConfirmation("Are you sure? All products in this category must be reassigned first.");
        if (!confirm) return;

        try {
            boolean ok = categoryService.deleteCategory(id);
            if (ok) System.out.println(AnsiColor.success("Category deleted."));
            else System.out.println(AnsiColor.error("Failed to delete category."));
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Cannot delete category with associated products: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------
    // Inventory Management
    // -------------------------------------------------------------
    private void handleInventoryManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + AnsiColor.info("--- [ADMIN] Inventory & Stock Control ---"));
            System.out.println("1. View Low Stock Alerts (Stock <= Threshold)");
            System.out.println("2. Adjust Stock Quantity (+/- delta)");
            System.out.println("3. Set Exact Stock Quantity");
            System.out.println("4. Back to Admin Menu");

            int option = InputValidator.readInt("Select", 1, 4);
            switch (option) {
                case 1 -> {
                    int threshold = InputValidator.readInt("Enter low stock threshold limit", 1, 500);
                    try {
                        List<Product> lowStock = productService.getLowStockProducts(threshold);
                        if (lowStock.isEmpty()) {
                            System.out.println(AnsiColor.success("No products are running low (all > " + threshold + ")!"));
                        } else {
                            ConsoleTable table = new ConsoleTable("ID", "SKU", "Name", "Stock Available");
                            for (Product p : lowStock) {
                                table.addRow(String.valueOf(p.getId()), p.getSku(), p.getName(),
                                        AnsiColor.color(String.valueOf(p.getStockQuantity()), AnsiColor.RED));
                            }
                            System.out.println(AnsiColor.warning("LOW STOCK ALERT:"));
                            table.print();
                        }
                    } catch (SQLException e) {
                        System.out.println(AnsiColor.error("Error: " + e.getMessage()));
                    }
                }
                case 2 -> {
                    listAllProducts(false);
                    int id = InputValidator.readInt("Enter Product ID to adjust stock", 1, Integer.MAX_VALUE);
                    int delta = InputValidator.readInt("Enter adjustment amount (positive to add, negative to reduce)", -10000, 10000);
                    try {
                        boolean ok = productService.adjustStock(id, delta);
                        if (ok) System.out.println(AnsiColor.success("Stock adjusted successfully!"));
                    } catch (Exception e) {
                        System.out.println(AnsiColor.error("Failed: " + e.getMessage()));
                    }
                }
                case 3 -> {
                    listAllProducts(false);
                    int id = InputValidator.readInt("Enter Product ID to set stock", 1, Integer.MAX_VALUE);
                    int newStock = InputValidator.readInt("Enter new exact stock quantity", 0, 1000000);
                    try {
                        boolean ok = productService.setStock(id, newStock);
                        if (ok) System.out.println(AnsiColor.success("Stock set successfully!"));
                    } catch (Exception e) {
                        System.out.println(AnsiColor.error("Failed: " + e.getMessage()));
                    }
                }
                case 4 -> back = true;
            }
        }
    }

    // -------------------------------------------------------------
    // Order Management
    // -------------------------------------------------------------
    private void handleOrderManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + AnsiColor.info("--- [ADMIN] Order Management & Fulfillment ---"));
            System.out.println("1. View All Orders");
            System.out.println("2. View Specific Order Details");
            System.out.println("3. Update Order Status (Processing / Shipped / Delivered)");
            System.out.println("4. Cancel Order (Restores Inventory)");
            System.out.println("5. Back to Admin Menu");

            int option = InputValidator.readInt("Select", 1, 5);
            switch (option) {
                case 1 -> listAllOrders();
                case 2 -> viewOrderDetails();
                case 3 -> updateOrderStatus();
                case 4 -> cancelOrderAdmin();
                case 5 -> back = true;
            }
        }
    }

    private void listAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            ConsoleTable table = new ConsoleTable("ID", "Order Number", "Customer", "Total", "Status", "Date");
            for (Order o : orders) {
                table.addRow(
                        String.valueOf(o.getId()),
                        o.getOrderNumber(),
                        o.getCustomerName() != null ? o.getCustomerName() : "ID:" + o.getCustomerId(),
                        "$" + o.getTotalAmount().toPlainString(),
                        formatStatus(o.getStatus()),
                        o.getCreatedAt() != null ? o.getCreatedAt().toString().substring(0, 19) : ""
                );
            }
            table.print();
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Error retrieving orders: " + e.getMessage()));
        }
    }

    private void viewOrderDetails() {
        int orderId = InputValidator.readInt("Enter Order ID", 1, Integer.MAX_VALUE);
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println(AnsiColor.error("Order not found."));
                return;
            }
            printOrderReceipt(orderOpt.get());
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database error: " + e.getMessage()));
        }
    }

    private void updateOrderStatus() {
        listAllOrders();
        int orderId = InputValidator.readInt("Enter Order ID to update", 1, Integer.MAX_VALUE);
        System.out.println("Available Statuses:");
        System.out.println("1. PENDING");
        System.out.println("2. PROCESSING");
        System.out.println("3. SHIPPED");
        System.out.println("4. DELIVERED");

        int sChoice = InputValidator.readInt("Select new status", 1, 4);
        OrderStatus newStatus = switch (sChoice) {
            case 1 -> OrderStatus.PENDING;
            case 2 -> OrderStatus.PROCESSING;
            case 3 -> OrderStatus.SHIPPED;
            case 4 -> OrderStatus.DELIVERED;
            default -> OrderStatus.PENDING;
        };

        try {
            boolean ok = orderService.updateOrderStatus(orderId, newStatus);
            if (ok) {
                System.out.println(AnsiColor.success("Order #" + orderId + " updated to status: " + newStatus));
            } else {
                System.out.println(AnsiColor.error("Failed to update status."));
            }
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Error: " + e.getMessage()));
        }
    }

    private void cancelOrderAdmin() {
        listAllOrders();
        int orderId = InputValidator.readInt("Enter Order ID to cancel", 1, Integer.MAX_VALUE);
        boolean confirm = InputValidator.readConfirmation("Cancel Order #" + orderId + " and restock inventory?");
        if (!confirm) return;

        try {
            boolean ok = orderService.cancelOrder(orderId);
            if (ok) {
                System.out.println(AnsiColor.success("Order #" + orderId + " cancelled and items restocked into inventory!"));
            }
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Cancellation failed: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------
    // Customer Management
    // -------------------------------------------------------------
    private void handleCustomerManagement() {
        try {
            List<User> customers = customerService.getAllCustomers();
            ConsoleTable table = new ConsoleTable("ID", "Username", "Full Name", "Email", "Phone", "Registered");
            for (User c : customers) {
                table.addRow(
                        String.valueOf(c.getId()),
                        c.getUsername(),
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhone() != null ? c.getPhone() : "-",
                        c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : ""
                );
            }
            System.out.println("\n" + AnsiColor.bold("Registered Customer Accounts:"));
            table.print();

            boolean inspect = InputValidator.readConfirmation("Inspect customer order history?");
            if (inspect) {
                int custId = InputValidator.readInt("Enter Customer ID", 1, Integer.MAX_VALUE);
                List<Order> orders = orderService.getCustomerOrders(custId);
                if (orders.isEmpty()) {
                    System.out.println(AnsiColor.warning("No orders placed yet by customer #" + custId));
                } else {
                    ConsoleTable ordTable = new ConsoleTable("Order Number", "Total", "Status", "Date");
                    for (Order o : orders) {
                        ordTable.addRow(o.getOrderNumber(), "$" + o.getTotalAmount(), formatStatus(o.getStatus()),
                                o.getCreatedAt().toString().substring(0, 19));
                    }
                    ordTable.print();
                }
            }
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Error: " + e.getMessage()));
        }
    }

    private String formatStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> AnsiColor.color("PENDING", AnsiColor.YELLOW);
            case PROCESSING -> AnsiColor.color("PROCESSING", AnsiColor.CYAN);
            case SHIPPED -> AnsiColor.color("SHIPPED", AnsiColor.BLUE);
            case DELIVERED -> AnsiColor.color("DELIVERED", AnsiColor.GREEN);
            case CANCELLED -> AnsiColor.color("CANCELLED", AnsiColor.RED);
        };
    }

    public static void printOrderReceipt(Order order) {
        System.out.println("\n" + AnsiColor.bold("=================================================="));
        System.out.println("                 ORDER INVOICE");
        System.out.println("==================================================");
        System.out.println(" Order Number     : " + AnsiColor.bold(order.getOrderNumber()));
        System.out.println(" Customer         : " + order.getCustomerName() + " (" + order.getCustomerEmail() + ")");
        System.out.println(" Order Date       : " + order.getCreatedAt());
        System.out.println(" Order Status     : " + order.getStatus());
        System.out.println(" Payment Method   : " + order.getPaymentMethod());
        System.out.println(" Shipping Address : " + order.getShippingAddress());
        System.out.println("--------------------------------------------------");

        ConsoleTable itemTable = new ConsoleTable("SKU", "Item Description", "Unit Price", "Qty", "Subtotal");
        for (OrderItem item : order.getItems()) {
            itemTable.addRow(
                    item.getProductSku(),
                    item.getProductName(),
                    "$" + item.getUnitPrice(),
                    String.valueOf(item.getQuantity()),
                    "$" + item.getSubtotal()
            );
        }
        itemTable.print();

        System.out.println(" Grand Total Paid : " + AnsiColor.success("$" + order.getTotalAmount().toPlainString()));
        System.out.println("==================================================\n");
    }
}
