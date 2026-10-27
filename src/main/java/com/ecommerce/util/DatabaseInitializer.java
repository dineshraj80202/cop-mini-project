package com.ecommerce.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes database tables and seed data if they do not exist.
 */
public class DatabaseInitializer {

    private static final String[] REQUIRED_TABLES = {
            "users", "categories", "products", "carts", "cart_items", "orders", "order_items"
    };

    /**
     * Checks if the required tables exist; if missing, runs schema.sql and seed_data.sql.
     */
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean tablesExist = checkTablesExist(conn);
            if (!tablesExist) {
                System.out.println(AnsiColor.info("Database tables not detected. Initializing schema..."));
                executeSqlFile(conn, "schema.sql");
                System.out.println(AnsiColor.success("Database schema created successfully."));

                System.out.println(AnsiColor.info("Populating initial seed data (users, categories, products)..."));
                executeSqlFile(conn, "seed_data.sql");
                System.out.println(AnsiColor.success("Initial seed data inserted successfully."));
            } else {
                // Run non-destructive migrations on existing database
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("UPDATE categories SET name = 'Clothes' WHERE name = 'Apparel'");
                } catch (Exception ignored) {}
                executeSqlFile(conn, "seed_data.sql");
            }
        } catch (SQLException e) {
            System.err.println(AnsiColor.warning("Database initialization check skipped or failed: " + e.getMessage()));
        }
    }

    /**
     * Verifies whether all required tables exist in the target database.
     */
    public static boolean checkTablesExist(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String table : REQUIRED_TABLES) {
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, table, new String[]{"TABLE"})) {
                if (!rs.next()) {
                    // Try lowercase/uppercase fallback
                    try (ResultSet rs2 = meta.getTables(conn.getCatalog(), null, table.toUpperCase(), new String[]{"TABLE"})) {
                        if (!rs2.next()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Reads a SQL script file from resources or file system, splits statements, and executes them.
     */
    public static void executeSqlFile(Connection conn, String fileName) {
        List<String> statements = loadSqlStatements(fileName);
        if (statements.isEmpty()) {
            System.err.println(AnsiColor.warning("SQL file not found or empty: " + fileName));
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Ignore already exists / duplicate key warnings in seed
                        String msg = e.getMessage().toLowerCase();
                        if (!msg.contains("already exists") && !msg.contains("duplicate entry")) {
                            System.err.println(AnsiColor.warning("Notice on SQL: " + e.getMessage()));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(AnsiColor.error("Failed executing SQL script: " + e.getMessage()));
        }
    }

    private static List<String> loadSqlStatements(String fileName) {
        List<String> statements = new ArrayList<>();
        InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName);

        if (is == null) {
            File[] searchLocations = new File[]{
                    new File("src/main/resources/" + fileName),
                    new File("database/" + fileName),
                    new File(fileName)
            };
            for (File file : searchLocations) {
                if (file.exists()) {
                    try {
                        is = new FileInputStream(file);
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }

        if (is == null) {
            return statements;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                    continue; // Skip full-line comments and empty lines
                }
                // Strip inline comment if present
                if (trimmed.contains("--")) {
                    trimmed = trimmed.substring(0, trimmed.indexOf("--")).trim();
                }
                currentStatement.append(trimmed).append(" ");
                if (trimmed.endsWith(";")) {
                    String stmtStr = currentStatement.toString().trim();
                    currentStatement.setLength(0);
                    // Skip database creation/switch in embedded environments
                    if (!stmtStr.toUpperCase().startsWith("CREATE DATABASE") && !stmtStr.toUpperCase().startsWith("USE ")) {
                        statements.add(stmtStr);
                    }
                }
            }
            if (currentStatement.length() > 0 && !currentStatement.toString().trim().isEmpty()) {
                String stmtStr = currentStatement.toString().trim();
                if (!stmtStr.toUpperCase().startsWith("CREATE DATABASE") && !stmtStr.toUpperCase().startsWith("USE ")) {
                    statements.add(stmtStr);
                }
            }
        } catch (Exception e) {
            System.err.println(AnsiColor.error("Error reading SQL file " + fileName + ": " + e.getMessage()));
        }
        return statements;
    }
}
