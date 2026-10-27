package com.ecommerce.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Database Connection Manager for MySQL JDBC.
 * Supports production MySQL server with seamless embedded fallback for instant local testing.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    public static final String EMBEDDED_URL = "jdbc:h2:mem:ecommerce_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    public static final String EMBEDDED_USER = "sa";
    public static final String EMBEDDED_PASS = "";

    private static Properties properties = new Properties();
    private static boolean driverLoaded = false;
    private static boolean embeddedMode = false;

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {
        // Private constructor
    }

    public static synchronized void loadConfiguration() {
        boolean loaded = false;
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                loaded = true;
            }
        } catch (Exception ignored) {}

        if (!loaded) {
            File[] searchLocations = new File[]{
                    new File("src/main/resources/" + CONFIG_FILE),
                    new File(CONFIG_FILE)
            };
            for (File file : searchLocations) {
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        properties.load(fis);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        url = envUrl != null ? envUrl : properties.getProperty("db.url", 
                "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        user = envUser != null ? envUser : properties.getProperty("db.username", "root");
        password = envPassword != null ? envPassword : properties.getProperty("db.password", "root");

        loadDrivers();
    }

    private static void loadDrivers() {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {}
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException ignored) {}
            driverLoaded = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        loadDrivers();
        if (embeddedMode) {
            return DriverManager.getConnection(EMBEDDED_URL, EMBEDDED_USER, EMBEDDED_PASS);
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // Auto fallback to embedded mode if MySQL is not running on localhost
            if (!embeddedMode && isLocalHostUrl(url)) {
                enableEmbeddedMode();
                System.out.println(AnsiColor.warning("Local MySQL on port 3306 is not reachable."));
                System.out.println(AnsiColor.info("Seamlessly switched to embedded MySQL-mode database for instant zero-setup execution!"));
                return DriverManager.getConnection(EMBEDDED_URL, EMBEDDED_USER, EMBEDDED_PASS);
            }
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static synchronized void enableEmbeddedMode() {
        embeddedMode = true;
    }

    public static synchronized void disableEmbeddedMode() {
        embeddedMode = false;
    }

    public static boolean isEmbeddedMode() {
        return embeddedMode;
    }

    public static synchronized void updateCredentials(String newUrl, String newUser, String newPassword) {
        if (newUrl != null && !newUrl.trim().isEmpty()) url = newUrl.trim();
        if (newUser != null && !newUser.trim().isEmpty()) user = newUser.trim();
        if (newPassword != null) password = newPassword.trim();
        embeddedMode = false;
    }

    public static String getUrl() {
        return embeddedMode ? EMBEDDED_URL : url;
    }

    public static String getUser() {
        return embeddedMode ? EMBEDDED_USER : user;
    }

    public static Properties getProperties() {
        return properties;
    }

    private static boolean isLocalHostUrl(String dbUrl) {
        return dbUrl != null && (dbUrl.contains("localhost") || dbUrl.contains("127.0.0.1"));
    }
}
