import java.sql.*;
import java.io.*;

public class SchemaTest2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:mem:ecommerce_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            runScript(c, "database/schema.sql");
            runScript(c, "database/seed_data.sql");
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) System.out.println("Products loaded: " + rs.getInt(1));
            }
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) System.out.println("Users loaded: " + rs.getInt(1));
            }
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next()) System.out.println("Categories loaded: " + rs.getInt(1));
            }
        }
    }

    static void runScript(Connection conn, String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.isEmpty()) continue;
            if (trimmed.contains("--")) {
                trimmed = trimmed.substring(0, trimmed.indexOf("--")).trim();
            }
            sb.append(trimmed).append(" ");
            if (trimmed.endsWith(";")) {
                String sql = sb.toString().trim();
                sb.setLength(0);
                if (sql.toUpperCase().startsWith("CREATE DATABASE") || sql.toUpperCase().startsWith("USE ")) continue;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                } catch (Exception e) {
                    System.out.println("Failed SQL: " + sql.substring(0, Math.min(60, sql.length())) + " -> " + e.getMessage());
                }
            }
        }
        reader.close();
    }
}