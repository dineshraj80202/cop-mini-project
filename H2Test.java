import java.sql.*;
public class H2Test {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:mem:ecommerce_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            System.out.println("Connected to H2 in MySQL mode: " + !c.isClosed());
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE test (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50))");
                s.execute("INSERT INTO test (name) VALUES ('Test Product')");
                try (ResultSet rs = s.executeQuery("SELECT * FROM test")) {
                    while (rs.next()) System.out.println("Row: " + rs.getInt("id") + " " + rs.getString("name"));
                }
            }
        }
    }
}