package com.ecommerce.ui;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Category;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.CartService;
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
 * Console UI controller for the Customer Storefront portal.
 */
public class CustomerMenu {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final AuthService authService;

    public CustomerMenu() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.cartService = new CartService();
        this.orderService = new OrderService();
        this.customerService = new CustomerService();
        this.authService = new AuthService();
    }

    public void display() {
        while (UserSession.isLoggedIn() && !UserSession.isAdmin()) {
            User user = UserSession.getCurrentUser();
            int cartItemCount = 0;
            try {
                Cart c = cartService.getCart(user.getId());
                cartItemCount = c.getTotalItemCount();
            } catch (SQLException ignored) {}

            System.out.println("\n" + AnsiColor.bold("=================================================="));
            System.out.println(AnsiColor.color("               CUSTOMER STOREFRONT", AnsiColor.BOLD_CYAN));
            System.out.println(AnsiColor.bold("=================================================="));
            System.out.println(" Welcome, " + AnsiColor.bold(user.getFullName()) +
                    " | Cart: " + AnsiColor.color("[" + cartItemCount + " items]", AnsiColor.BOLD_YELLOW));
            System.out.println(" 1. Browse Product Catalog");
            System.out.println(" 2. Search & Filter Products");
            System.out.println(" 3. View Shopping Cart & Checkout");
            System.out.println(" 4. Order History & Tracking");
            System.out.println(" 5. My Account Profile");
            System.out.println(" 6. Logout");
            System.out.println("==================================================");

            int choice = InputValidator.readInt("Select Option", 1, 6);
            switch (choice) {
                case 1 -> browseProducts();
                case 2 -> searchProducts();
                case 3 -> handleCart();
                case 4 -> handleOrderHistory();
                case 5 -> handleProfile();
                case 6 -> {
                    UserSession.clear();
                    System.out.println(AnsiColor.success("Logged out successfully. Thank you for shopping with us!"));
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Catalog Browsing & Search
    // -------------------------------------------------------------
    public void browseProducts() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + AnsiColor.info("--- Browse Products ---"));
            System.out.println("1. All Active Products");
            System.out.println("2. Filter by Category");
            System.out.println("3. View Product Details & Add to Cart");
            System.out.println("4. Back");

            int opt = InputValidator.readInt("Select", 1, 4);
            switch (opt) {
                case 1 -> {
                    try {
                        renderProductList(productService.getAllProducts(false));
                    } catch (SQLException e) {
                        System.out.println(AnsiColor.error("Error: " + e.getMessage()));
                    }
                }
                case 2 -> filterByCategory();
                case 3 -> viewProductDetailsAndAddToCart();
                case 4 -> back = true;
            }
        }
    }

    private void filterByCategory() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            ConsoleTable table = new ConsoleTable("ID", "Category Name");
            for (Category c : categories) {
                table.addRow(String.valueOf(c.getId()), c.getName());
            }
            table.print();
            int catId = InputValidator.readInt("Select Category ID", 1, Integer.MAX_VALUE);
            List<Product> products = productService.getProductsByCategory(catId);
            renderProductList(products);
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Error: " + e.getMessage()));
        }
    }

    public void searchProducts() {
        System.out.println("\n" + AnsiColor.info("--- Search Products ---"));
        String keyword = InputValidator.readString("Keyword (name, SKU, description - or leave blank)", true);

        String minStr = InputValidator.readString("Minimum Price (or leave blank)", true);
        BigDecimal minPrice = null;
        if (!minStr.isEmpty()) {
            try { minPrice = new BigDecimal(minStr); } catch (Exception ignored) {}
        }

        String maxStr = InputValidator.readString("Maximum Price (or leave blank)", true);
        BigDecimal maxPrice = null;
        if (!maxStr.isEmpty()) {
            try { maxPrice = new BigDecimal(maxStr); } catch (Exception ignored) {}
        }

        try {
            List<Product> results = productService.searchProducts(
                    keyword.isEmpty() ? null : keyword,
                    null,
                    minPrice,
                    maxPrice
            );
            renderProductList(results);
            if (!results.isEmpty() && UserSession.isLoggedIn()) {
                boolean add = InputValidator.readConfirmation("Would you like to add an item to your cart?");
                if (add) {
                    addToCartPrompt();
                }
            }
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Search error: " + e.getMessage()));
        }
    }

    private void renderProductList(List<Product> products) {
        ConsoleTable table = new ConsoleTable("ID", "SKU", "Product Name", "Category", "Price", "Stock");
        for (Product p : products) {
            String stockDisplay = p.getStockQuantity() > 0 ? 
                    String.valueOf(p.getStockQuantity()) : 
                    AnsiColor.color("OUT OF STOCK", AnsiColor.RED);
            table.addRow(
                    String.valueOf(p.getId()),
                    p.getSku(),
                    p.getName(),
                    p.getCategoryName() != null ? p.getCategoryName() : "-",
                    "$" + p.getPrice().toPlainString(),
                    stockDisplay
            );
        }
        table.print();
    }

    private void viewProductDetailsAndAddToCart() {
        int prodId = InputValidator.readInt("Enter Product ID to view details", 1, Integer.MAX_VALUE);
        try {
            Optional<Product> prodOpt = productService.getProductById(prodId);
            if (prodOpt.isEmpty() || !prodOpt.get().isActive()) {
                System.out.println(AnsiColor.error("Product not available."));
                return;
            }
            Product p = prodOpt.get();
            System.out.println("\n" + AnsiColor.bold("--------------------------------------------------"));
            System.out.println("Product Name : " + AnsiColor.bold(p.getName()));
            System.out.println("SKU          : " + p.getSku());
            System.out.println("Category     : " + p.getCategoryName());
            System.out.println("Price        : " + AnsiColor.success("$" + p.getPrice()));
            System.out.println("Available    : " + (p.isInStock() ? p.getStockQuantity() + " units" : AnsiColor.error("Out of Stock")));
            System.out.println("Description  : " + p.getDescription());
            System.out.println(AnsiColor.bold("--------------------------------------------------"));

            if (UserSession.isLoggedIn() && !UserSession.isAdmin()) {
                if (p.isInStock()) {
                    boolean add = InputValidator.readConfirmation("Add this product to your cart?");
                    if (add) {
                        int qty = InputValidator.readInt("Quantity", 1, p.getStockQuantity());
                        cartService.addToCart(UserSession.getCurrentUser().getId(), p.getId(), qty);
                        System.out.println(AnsiColor.success("Item successfully added to your cart!"));
                    }
                } else {
                    System.out.println(AnsiColor.warning("This item is currently out of stock."));
                }
            }
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Error: " + e.getMessage()));
        }
    }

    private void addToCartPrompt() {
        int prodId = InputValidator.readInt("Enter Product ID to add", 1, Integer.MAX_VALUE);
        int qty = InputValidator.readInt("Enter Quantity", 1, 1000);
        try {
            cartService.addToCart(UserSession.getCurrentUser().getId(), prodId, qty);
            System.out.println(AnsiColor.success("Item successfully added to cart!"));
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Failed to add to cart: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------
    // Shopping Cart & Checkout
    // -------------------------------------------------------------
    private void handleCart() {
        boolean back = false;
        while (!back) {
            try {
                Cart cart = cartService.getCart(UserSession.getCurrentUser().getId());
                System.out.println("\n" + AnsiColor.info("--- Your Shopping Cart ---"));
                if (cart.isEmpty()) {
                    System.out.println(AnsiColor.warning("Your cart is currently empty."));
                    System.out.println("1. Browse Catalog");
                    System.out.println("2. Back");
                    int opt = InputValidator.readInt("Select", 1, 2);
                    if (opt == 1) browseProducts();
                    return;
                }

                ConsoleTable table = new ConsoleTable("Product ID", "SKU", "Product Name", "Unit Price", "Qty", "Subtotal");
                for (CartItem item : cart.getItems()) {
                    table.addRow(
                            String.valueOf(item.getProductId()),
                            item.getProductSku(),
                            item.getProductName(),
                            "$" + item.getUnitPrice(),
                            String.valueOf(item.getQuantity()),
                            "$" + item.getSubtotal()
                    );
                }
                table.print();
                System.out.println(AnsiColor.bold("Total Amount: ") + AnsiColor.success("$" + cart.getTotalAmount()));

                System.out.println("\n1. Proceed to Checkout");
                System.out.println("2. Update Item Quantity");
                System.out.println("3. Remove Item from Cart");
                System.out.println("4. Clear Cart");
                System.out.println("5. Back to Main Store");

                int opt = InputValidator.readInt("Select", 1, 5);
                switch (opt) {
                    case 1 -> {
                        checkoutFlow(cart);
                        return;
                    }
                    case 2 -> {
                        int prodId = InputValidator.readInt("Enter Product ID to update", 1, Integer.MAX_VALUE);
                        int newQty = InputValidator.readInt("Enter new quantity (0 to remove)", 0, 1000);
                        cartService.updateQuantity(UserSession.getCurrentUser().getId(), prodId, newQty);
                        System.out.println(AnsiColor.success("Cart updated."));
                    }
                    case 3 -> {
                        int prodId = InputValidator.readInt("Enter Product ID to remove", 1, Integer.MAX_VALUE);
                        cartService.removeFromCart(UserSession.getCurrentUser().getId(), prodId);
                        System.out.println(AnsiColor.success("Item removed from cart."));
                    }
                    case 4 -> {
                        boolean confirm = InputValidator.readConfirmation("Clear all items in cart?");
                        if (confirm) {
                            cartService.clearCart(UserSession.getCurrentUser().getId());
                            System.out.println(AnsiColor.success("Cart cleared."));
                        }
                    }
                    case 5 -> back = true;
                }
            } catch (Exception e) {
                System.out.println(AnsiColor.error("Cart error: " + e.getMessage()));
                return;
            }
        }
    }

    private void checkoutFlow(Cart cart) {
        System.out.println("\n" + AnsiColor.bold("=================================================="));
        System.out.println(AnsiColor.color("               CHECKOUT & PAYMENT", AnsiColor.BOLD_GREEN));
        System.out.println(AnsiColor.bold("=================================================="));
        System.out.println("Items in Cart: " + cart.getTotalItemCount());
        System.out.println("Cart Total   : " + AnsiColor.success("$" + cart.getTotalAmount()));

        User user = UserSession.getCurrentUser();
        String defaultAddr = user.getAddress() != null && !user.getAddress().isEmpty() ? 
                user.getAddress() : "123 Main St, Springfield";

        System.out.println("\nDefault shipping address on file: " + AnsiColor.bold(defaultAddr));
        boolean useDefault = InputValidator.readConfirmation("Use default shipping address?");
        String shippingAddress = defaultAddr;
        if (!useDefault) {
            shippingAddress = InputValidator.readString("Enter delivery shipping address", false);
        }

        System.out.println("\nSelect Payment Method:");
        System.out.println("1. Credit / Debit Card");
        System.out.println("2. PayPal");
        System.out.println("3. Cash on Delivery (COD)");
        int pChoice = InputValidator.readInt("Select", 1, 3);
        String paymentMethod = switch (pChoice) {
            case 1 -> "CREDIT_CARD";
            case 2 -> "PAYPAL";
            case 3 -> "CASH_ON_DELIVERY";
            default -> "CREDIT_CARD";
        };

        boolean confirmOrder = InputValidator.readConfirmation("Confirm and place order for $" + cart.getTotalAmount() + "?");
        if (!confirmOrder) {
            System.out.println(AnsiColor.warning("Checkout cancelled."));
            return;
        }

        try {
            Order order = orderService.checkout(user.getId(), shippingAddress, paymentMethod);
            System.out.println(AnsiColor.success("\n🎉 Order successfully placed!"));
            AdminMenu.printOrderReceipt(order);
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Order processing failed (Transaction rolled back): " + e.getMessage()));
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Failed to place order: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------
    // Order History & Tracking
    // -------------------------------------------------------------
    private void handleOrderHistory() {
        try {
            int custId = UserSession.getCurrentUser().getId();
            List<Order> orders = orderService.getCustomerOrders(custId);
            if (orders.isEmpty()) {
                System.out.println(AnsiColor.warning("You have not placed any orders yet."));
                return;
            }

            ConsoleTable table = new ConsoleTable("ID", "Order Number", "Total", "Status", "Date");
            for (Order o : orders) {
                table.addRow(
                        String.valueOf(o.getId()),
                        o.getOrderNumber(),
                        "$" + o.getTotalAmount(),
                        o.getStatus().name(),
                        o.getCreatedAt().toString().substring(0, 19)
                );
            }
            System.out.println("\n" + AnsiColor.bold("Your Order History:"));
            table.print();

            System.out.println("1. View Full Order Invoice/Receipt");
            System.out.println("2. Cancel Pending Order");
            System.out.println("3. Back");

            int opt = InputValidator.readInt("Select", 1, 3);
            if (opt == 1) {
                int ordId = InputValidator.readInt("Enter Order ID", 1, Integer.MAX_VALUE);
                Optional<Order> ordOpt = orderService.getOrderById(ordId);
                if (ordOpt.isPresent() && ordOpt.get().getCustomerId() == custId) {
                    AdminMenu.printOrderReceipt(ordOpt.get());
                } else {
                    System.out.println(AnsiColor.error("Order not found or access denied."));
                }
            } else if (opt == 2) {
                int ordId = InputValidator.readInt("Enter Order ID to cancel", 1, Integer.MAX_VALUE);
                Optional<Order> ordOpt = orderService.getOrderById(ordId);
                if (ordOpt.isPresent() && ordOpt.get().getCustomerId() == custId) {
                    Order o = ordOpt.get();
                    if (o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.PROCESSING) {
                        System.out.println(AnsiColor.error("Only PENDING or PROCESSING orders can be cancelled. Current status: " + o.getStatus()));
                        return;
                    }
                    boolean confirm = InputValidator.readConfirmation("Cancel Order " + o.getOrderNumber() + "?");
                    if (confirm) {
                        orderService.cancelOrder(ordId);
                        System.out.println(AnsiColor.success("Order cancelled. Inventory has been restored."));
                    }
                } else {
                    System.out.println(AnsiColor.error("Order not found."));
                }
            }
        } catch (Exception e) {
            System.out.println(AnsiColor.error("Error: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------
    // Customer Profile
    // -------------------------------------------------------------
    private void handleProfile() {
        User user = UserSession.getCurrentUser();
        System.out.println("\n" + AnsiColor.info("--- My Account Profile ---"));
        System.out.println("Username : " + user.getUsername());
        System.out.println("Full Name: " + user.getFullName());
        System.out.println("Email    : " + user.getEmail());
        System.out.println("Phone    : " + user.getPhone());
        System.out.println("Address  : " + user.getAddress());

        System.out.println("\n1. Update Profile (Name, Phone, Address)");
        System.out.println("2. Change Password");
        System.out.println("3. Back");

        int opt = InputValidator.readInt("Select", 1, 3);
        if (opt == 1) {
            String name = InputValidator.readString("New Full Name [" + user.getFullName() + "]", true);
            String phone = InputValidator.readString("New Phone [" + user.getPhone() + "]", true);
            String addr = InputValidator.readString("New Address [" + user.getAddress() + "]", true);

            try {
                boolean ok = customerService.updateProfile(
                        user.getId(),
                        name.isEmpty() ? null : name,
                        phone.isEmpty() ? null : phone,
                        addr.isEmpty() ? null : addr
                );
                if (ok) {
                    // Refresh session
                    customerService.getCustomerById(user.getId()).ifPresent(UserSession::setCurrentUser);
                    System.out.println(AnsiColor.success("Profile updated successfully!"));
                }
            } catch (SQLException e) {
                System.out.println(AnsiColor.error("Update failed: " + e.getMessage()));
            }
        } else if (opt == 2) {
            String oldPass = InputValidator.readPassword("Current Password");
            String newPass = InputValidator.readPassword("New Password (min 6 characters)");
            try {
                boolean ok = authService.changePassword(user.getId(), oldPass, newPass);
                if (ok) {
                    System.out.println(AnsiColor.success("Password changed successfully!"));
                } else {
                    System.out.println(AnsiColor.error("Incorrect current password."));
                }
            } catch (Exception e) {
                System.out.println(AnsiColor.error("Error changing password: " + e.getMessage()));
            }
        }
    }
}
