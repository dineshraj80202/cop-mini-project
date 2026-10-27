package com.ecommerce.ui;

import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.AnsiColor;
import com.ecommerce.util.DatabaseConnection;
import com.ecommerce.util.DatabaseInitializer;
import com.ecommerce.util.InputValidator;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Top-level application menu controller for authentication, guest browsing, and database setup.
 */
public class MainMenu {
    private final AuthService authService;
    private final AdminMenu adminMenu;
    private final CustomerMenu customerMenu;

    public MainMenu() {
        this.authService = new AuthService();
        this.adminMenu = new AdminMenu();
        this.customerMenu = new CustomerMenu();
    }

    public void start() {
        printBanner();
        checkDatabaseStatus();

        boolean running = true;
        while (running) {
            System.out.println("\n" + AnsiColor.bold("=================================================="));
            System.out.println(AnsiColor.color("               MAIN NAVIGATION MENU", AnsiColor.BOLD_BLUE));
            System.out.println(AnsiColor.bold("=================================================="));
            System.out.println(" 1. Sign In (Customer / Administrator)");
            System.out.println(" 2. Create New Customer Account (Register)");
            System.out.println(" 3. Browse Products as Guest");
            System.out.println(" 4. Database Connection & System Diagnostics");
            System.out.println(" 5. Exit");
            System.out.println("==================================================");

            int choice = InputValidator.readInt("Select Option", 1, 5);
            switch (choice) {
                case 1 -> handleLogin();
                case 2 -> handleRegistration();
                case 3 -> handleGuestBrowsing();
                case 4 -> handleDatabaseConfig();
                case 5 -> {
                    System.out.println(AnsiColor.info("\nThank you for using the E-Commerce Product Management System. Goodbye!"));
                    running = false;
                }
            }
        }
    }

    private void printBanner() {
        System.out.println(AnsiColor.color(
                "\n" +
                "  ______                                                        \n" +
                " |  ____|                                                       \n" +
                " | |__     ______ ___  _ __ ___  _ __ ___   ___ _ __ ___  ___   \n" +
                " |  __|   |______/ __|| '_ ` _ \\| '_ ` _ \\ / _ \\ '__/ __|/ _ \\  \n" +
                " | |____        | (__ | | | | | | | | | | |  __/ | | (__|  __/  \n" +
                " |______|        \\___||_| |_| |_|_| |_| |_|\\___|_|  \\___|\\___|  \n" +
                "                                                                \n" +
                "         PRODUCT MANAGEMENT & COMMERCE SYSTEM (JDBC + MySQL)    \n",
                AnsiColor.BOLD_CYAN
        ));
    }

    private void checkDatabaseStatus() {
        System.out.print("Testing MySQL connection... ");
        if (DatabaseConnection.testConnection()) {
            System.out.println(AnsiColor.success("[CONNECTED]"));
            DatabaseInitializer.initializeDatabase();
        } else {
            System.out.println(AnsiColor.warning("[OFFLINE / NOT REACHABLE]"));
            System.out.println(AnsiColor.warning("Notice: Could not connect to MySQL at: " + DatabaseConnection.getUrl()));
            System.out.println(AnsiColor.info("You can configure database credentials from Option 4."));
        }
    }

    private void handleLogin() {
        System.out.println("\n" + AnsiColor.info("--- Sign In ---"));
        System.out.println("(Default Admin demo: admin / admin123 | Customer demo: john_doe / user123)");
        String username = InputValidator.readString("Username or Email", false);
        String password = InputValidator.readPassword("Password");

        try {
            Optional<User> userOpt = authService.login(username, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserSession.setCurrentUser(user);
                System.out.println(AnsiColor.success("\nLogin successful! Welcome back, " + user.getFullName() + "."));

                if (user.isAdmin()) {
                    adminMenu.display();
                } else {
                    customerMenu.display();
                }
            } else {
                System.out.println(AnsiColor.error("Invalid username/email or password. Please try again."));
            }
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database connection error during login: " + e.getMessage()));
            System.out.println(AnsiColor.warning("Tip: Use Option 4 to check or update MySQL connection settings."));
        }
    }

    private void handleRegistration() {
        System.out.println("\n" + AnsiColor.info("--- New Customer Registration ---"));
        String username = InputValidator.readString("Desired Username", false);
        String password = InputValidator.readPassword("Password (min 6 characters)");
        String fullName = InputValidator.readString("Full Name", false);
        String email = InputValidator.readEmail("Email Address");
        String phone = InputValidator.readString("Phone Number", true);
        String address = InputValidator.readString("Shipping Address", true);

        try {
            User registered = authService.register(username, password, fullName, email, phone, address);
            System.out.println(AnsiColor.success("Account registered successfully! You can now sign in with your credentials."));
        } catch (IllegalArgumentException e) {
            System.out.println(AnsiColor.error("Registration failed: " + e.getMessage()));
        } catch (SQLException e) {
            System.out.println(AnsiColor.error("Database error during registration: " + e.getMessage()));
        }
    }

    private void handleGuestBrowsing() {
        System.out.println("\n" + AnsiColor.info("--- Browsing Catalog as Guest ---"));
        customerMenu.browseProducts();
    }

    private void handleDatabaseConfig() {
        System.out.println("\n" + AnsiColor.info("--- Database Connection & Diagnostics ---"));
        System.out.println("Current URL : " + DatabaseConnection.getUrl());
        System.out.println("Current User: " + DatabaseConnection.getUser());

        boolean isConn = DatabaseConnection.testConnection();
        System.out.println("Status      : " + (isConn ? AnsiColor.success("CONNECTED") : AnsiColor.error("DISCONNECTED")));

        System.out.println("\n1. Test Connection Again");
        System.out.println("2. Update Connection Settings (Host, Port, User, Password, Database)");
        System.out.println("3. Run Schema & Seed Initializer");
        System.out.println("4. Back to Main Menu");

        int opt = InputValidator.readInt("Select", 1, 4);
        switch (opt) {
            case 1 -> {
                if (DatabaseConnection.testConnection()) {
                    System.out.println(AnsiColor.success("Database connection successful!"));
                } else {
                    System.out.println(AnsiColor.error("Database connection failed. Please verify MySQL service is running."));
                }
            }
            case 2 -> {
                String host = InputValidator.readString("MySQL Host [localhost]", true);
                if (host.isEmpty()) host = "localhost";

                String port = InputValidator.readString("MySQL Port [3306]", true);
                if (port.isEmpty()) port = "3306";

                String db = InputValidator.readString("Database Name [ecommerce_db]", true);
                if (db.isEmpty()) db = "ecommerce_db";

                String user = InputValidator.readString("Username [root]", true);
                if (user.isEmpty()) user = "root";

                String pass = InputValidator.readPassword("Password");

                String newUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + 
                        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";

                DatabaseConnection.updateCredentials(newUrl, user, pass);
                System.out.println(AnsiColor.info("Updated credentials. Testing..."));
                if (DatabaseConnection.testConnection()) {
                    System.out.println(AnsiColor.success("Successfully connected to MySQL!"));
                    DatabaseInitializer.initializeDatabase();
                } else {
                    System.out.println(AnsiColor.error("Could not connect with new settings."));
                }
            }
            case 3 -> {
                if (DatabaseConnection.testConnection()) {
                    DatabaseInitializer.initializeDatabase();
                    System.out.println(AnsiColor.success("Database initialization completed."));
                } else {
                    System.out.println(AnsiColor.error("Cannot run initializer: database is not connected."));
                }
            }
            case 4 -> {}
        }
    }
}
