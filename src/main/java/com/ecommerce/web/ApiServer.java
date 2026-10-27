package com.ecommerce.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.ecommerce.util.DatabaseInitializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Embedded HTTP REST API and Static Web Server powered by JDK HttpServer.
 */
public class ApiServer {
    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();
    private final AuthService authService = new AuthService();
    private final CustomerService customerService = new CustomerService();

    public ApiServer() {
        this(DEFAULT_PORT);
    }

    public ApiServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // Ensure database tables exist
        DatabaseInitializer.initializeDatabase();

        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Handler
        server.createContext("/api", new ApiHandler());

        // Static Web Asset Handler (serving frontend)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println(AnsiColor.success("=================================================="));
        System.out.println(AnsiColor.color("   E-COMMERCE FULL-STACK WEB SERVER RUNNING", AnsiColor.BOLD_GREEN));
        System.out.println(AnsiColor.success("=================================================="));
        System.out.println("  Access Web Storefront & Admin: " + AnsiColor.bold("http://localhost:" + port));
        System.out.println("  REST API Base Endpoint       : " + AnsiColor.bold("http://localhost:" + port + "/api"));
        System.out.println("==================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        ApiServer appServer = new ApiServer(port);
        appServer.start();
    }

    // -------------------------------------------------------------
    // REST API Request Router
    // -------------------------------------------------------------
    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod().toUpperCase();
            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String body = readBody(exchange);

            try {
                // Auth endpoints
                if (path.equals("/api/auth/login") && method.equals("POST")) {
                    handleLogin(exchange, body);
                } else if (path.equals("/api/auth/register") && method.equals("POST")) {
                    handleRegister(exchange, body);
                }
                // Categories
                else if (path.equals("/api/categories") && method.equals("GET")) {
                    sendJsonResponse(exchange, 200, categoryService.getAllCategories());
                } else if (path.equals("/api/categories") && method.equals("POST")) {
                    handleCreateCategory(exchange, body);
                } else if (path.matches("^/api/categories/\\d+$") && method.equals("PUT")) {
                    handleUpdateCategory(exchange, extractIdFromPath(path), body);
                } else if (path.matches("^/api/categories/\\d+$") && method.equals("DELETE")) {
                    handleDeleteCategory(exchange, extractIdFromPath(path));
                }
                // Products
                else if (path.equals("/api/products") && method.equals("GET")) {
                    handleGetProducts(exchange, queryParams);
                } else if (path.matches("^/api/products/\\d+$") && method.equals("GET")) {
                    int id = extractIdFromPath(path);
                    Optional<Product> p = productService.getProductById(id);
                    if (p.isPresent()) sendJsonResponse(exchange, 200, p.get());
                    else sendError(exchange, 404, "Product not found");
                } else if (path.equals("/api/products") && method.equals("POST")) {
                    handleCreateProduct(exchange, body);
                } else if (path.matches("^/api/products/\\d+$") && method.equals("PUT")) {
                    handleUpdateProduct(exchange, extractIdFromPath(path), body);
                } else if (path.matches("^/api/products/\\d+$") && method.equals("DELETE")) {
                    handleDeleteProduct(exchange, extractIdFromPath(path));
                } else if (path.matches("^/api/products/\\d+/stock$") && method.equals("POST")) {
                    handleAdjustStock(exchange, extractIdFromPath(path), body);
                }
                // Cart
                else if (path.equals("/api/cart") && method.equals("GET")) {
                    int custId = Integer.parseInt(queryParams.getOrDefault("customerId", "2"));
                    sendJsonResponse(exchange, 200, cartService.getCart(custId));
                } else if (path.equals("/api/cart/add") && method.equals("POST")) {
                    handleAddToCart(exchange, body);
                } else if (path.equals("/api/cart/update") && method.equals("POST")) {
                    handleUpdateCart(exchange, body);
                } else if (path.equals("/api/cart/remove") && method.equals("POST")) {
                    handleRemoveCartItem(exchange, body);
                } else if (path.equals("/api/cart/clear") && method.equals("POST")) {
                    handleClearCart(exchange, body);
                }
                // Orders
                else if (path.equals("/api/orders/checkout") && method.equals("POST")) {
                    handleCheckout(exchange, body);
                } else if (path.equals("/api/orders") && method.equals("GET")) {
                    handleGetOrders(exchange, queryParams);
                } else if (path.matches("^/api/orders/\\d+$") && method.equals("GET")) {
                    int id = extractIdFromPath(path);
                    Optional<Order> o = orderService.getOrderById(id);
                    if (o.isPresent()) sendJsonResponse(exchange, 200, o.get());
                    else sendError(exchange, 404, "Order not found");
                } else if (path.matches("^/api/orders/\\d+/status$") && method.equals("POST")) {
                    handleUpdateOrderStatus(exchange, extractIdFromPath(path), body);
                } else if (path.matches("^/api/orders/\\d+/cancel$") && method.equals("POST")) {
                    handleCancelOrder(exchange, extractIdFromPath(path));
                }
                // Customers & Stats (Admin)
                else if (path.equals("/api/customers") && method.equals("GET")) {
                    sendJsonResponse(exchange, 200, customerService.getAllCustomers());
                } else if (path.equals("/api/stats") && method.equals("GET")) {
                    handleGetStats(exchange);
                } else {
                    sendError(exchange, 404, "Endpoint not found: " + path);
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database error: " + e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Server error: " + e.getMessage());
            }
        }

        private void handleLogin(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> data = JsonHelper.parseJsonObject(body);
            String username = (String) data.get("username");
            String password = (String) data.get("password");

            Optional<User> userOpt = authService.login(username, password);
            if (userOpt.isPresent()) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("user", userOpt.get());
                sendJsonResponse(exchange, 200, resp);
            } else {
                sendError(exchange, 401, "Invalid username or password");
            }
        }

        private void handleRegister(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            String username = (String) d.get("username");
            String password = (String) d.get("password");
            String fullName = (String) d.get("fullName");
            String email = (String) d.get("email");
            String phone = (String) d.get("phone");
            String address = (String) d.get("address");

            User u = authService.register(username, password, fullName, email, phone, address);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("user", u);
            sendJsonResponse(exchange, 201, resp);
        }

        private void handleGetProducts(HttpExchange exchange, Map<String, String> q) throws SQLException, IOException {
            String kw = q.get("keyword");
            String catIdStr = q.get("categoryId");
            String minPriceStr = q.get("minPrice");
            String maxPriceStr = q.get("maxPrice");
            boolean all = "true".equalsIgnoreCase(q.get("all"));

            if (kw != null || catIdStr != null || minPriceStr != null || maxPriceStr != null) {
                int catId = 0;
                boolean hasCategoryFilter = catIdStr != null && !catIdStr.isEmpty();
                if (hasCategoryFilter) {
                    catId = Integer.parseInt(catIdStr);
                }
                BigDecimal minP = minPriceStr != null && !minPriceStr.isEmpty() ? new BigDecimal(minPriceStr) : null;
                BigDecimal maxP = maxPriceStr != null && !maxPriceStr.isEmpty() ? new BigDecimal(maxPriceStr) : null;
                sendJsonResponse(exchange, 200, productService.searchProducts(kw, hasCategoryFilter ? catId : null, minP, maxP));
            } else {
                sendJsonResponse(exchange, 200, productService.getAllProducts(all));
            }
        }

        private void handleCreateProduct(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int catId = ((Number) d.get("categoryId")).intValue();
            String sku = (String) d.get("sku");
            String name = (String) d.get("name");
            String desc = (String) d.get("description");
            BigDecimal price = new BigDecimal(String.valueOf(d.get("price")));
            int stock = ((Number) d.get("stockQuantity")).intValue();

            Product p = productService.addProduct(catId, sku, name, desc, price, stock);
            sendJsonResponse(exchange, 201, p);
        }

        private void handleUpdateProduct(HttpExchange exchange, int id, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            Optional<Product> opt = productService.getProductById(id);
            if (opt.isEmpty()) {
                sendError(exchange, 404, "Product not found");
                return;
            }
            Product p = opt.get();
            if (d.containsKey("categoryId")) p.setCategoryId(((Number) d.get("categoryId")).intValue());
            if (d.containsKey("name")) p.setName((String) d.get("name"));
            if (d.containsKey("description")) p.setDescription((String) d.get("description"));
            if (d.containsKey("price")) p.setPrice(new BigDecimal(String.valueOf(d.get("price"))));
            if (d.containsKey("stockQuantity")) p.setStockQuantity(((Number) d.get("stockQuantity")).intValue());
            if (d.containsKey("active")) p.setActive(Boolean.parseBoolean(String.valueOf(d.get("active"))));

            productService.updateProduct(p);
            sendJsonResponse(exchange, 200, p);
        }

        private void handleDeleteProduct(HttpExchange exchange, int id) throws Exception {
            boolean ok = productService.deleteProduct(id);
            if (!ok) {
                // If linked to orders, deactivate instead
                productService.toggleProductStatus(id, false);
            }
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("success", true);
            sendJsonResponse(exchange, 200, r);
        }

        private void handleAdjustStock(HttpExchange exchange, int id, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int delta = ((Number) d.get("delta")).intValue();
            productService.adjustStock(id, delta);
            Product updated = productService.getProductById(id).orElseThrow();
            sendJsonResponse(exchange, 200, updated);
        }

        private void handleCreateCategory(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            String name = (String) d.get("name");
            String desc = (String) d.get("description");
            Category c = categoryService.createCategory(name, desc);
            sendJsonResponse(exchange, 201, c);
        }

        private void handleUpdateCategory(HttpExchange exchange, int id, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            String name = (String) d.get("name");
            String desc = (String) d.get("description");
            boolean ok = categoryService.updateCategory(id, name, desc);
            if (!ok) {
                sendError(exchange, 404, "Category not found");
                return;
            }
            Optional<Category> updated = categoryService.getCategoryById(id);
            sendJsonResponse(exchange, 200, updated.orElse(null));
        }

        private void handleDeleteCategory(HttpExchange exchange, int id) throws Exception {
            boolean ok = categoryService.deleteCategory(id);
            if (!ok) {
                sendError(exchange, 400, "Could not delete category (it may contain products or not exist).");
                return;
            }
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("success", true);
            sendJsonResponse(exchange, 200, r);
        }

        private void handleAddToCart(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int custId = ((Number) d.get("customerId")).intValue();
            int prodId = ((Number) d.get("productId")).intValue();
            int qty = d.containsKey("quantity") ? ((Number) d.get("quantity")).intValue() : 1;

            cartService.addToCart(custId, prodId, qty);
            sendJsonResponse(exchange, 200, cartService.getCart(custId));
        }

        private void handleUpdateCart(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int custId = ((Number) d.get("customerId")).intValue();
            int prodId = ((Number) d.get("productId")).intValue();
            int qty = ((Number) d.get("quantity")).intValue();

            cartService.updateQuantity(custId, prodId, qty);
            sendJsonResponse(exchange, 200, cartService.getCart(custId));
        }

        private void handleRemoveCartItem(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int custId = ((Number) d.get("customerId")).intValue();
            int prodId = ((Number) d.get("productId")).intValue();

            cartService.removeFromCart(custId, prodId);
            sendJsonResponse(exchange, 200, cartService.getCart(custId));
        }

        private void handleClearCart(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int custId = ((Number) d.get("customerId")).intValue();

            cartService.clearCart(custId);
            sendJsonResponse(exchange, 200, cartService.getCart(custId));
        }

        private void handleCheckout(HttpExchange exchange, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            int custId = ((Number) d.get("customerId")).intValue();
            String addr = (String) d.get("shippingAddress");
            String pay = (String) d.get("paymentMethod");

            Order order = orderService.checkout(custId, addr, pay);
            sendJsonResponse(exchange, 201, order);
        }

        private void handleGetOrders(HttpExchange exchange, Map<String, String> q) throws Exception {
            String custIdStr = q.get("customerId");
            if (custIdStr != null && !custIdStr.isEmpty()) {
                sendJsonResponse(exchange, 200, orderService.getCustomerOrders(Integer.parseInt(custIdStr)));
            } else {
                sendJsonResponse(exchange, 200, orderService.getAllOrders());
            }
        }

        private void handleUpdateOrderStatus(HttpExchange exchange, int id, String body) throws Exception {
            Map<String, Object> d = JsonHelper.parseJsonObject(body);
            String statusStr = (String) d.get("status");
            OrderStatus st = OrderStatus.fromString(statusStr);
            orderService.updateOrderStatus(id, st);
            sendJsonResponse(exchange, 200, orderService.getOrderById(id).orElseThrow());
        }

        private void handleCancelOrder(HttpExchange exchange, int id) throws Exception {
            orderService.cancelOrder(id);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("success", true);
            r.put("message", "Order cancelled and items restocked into inventory.");
            sendJsonResponse(exchange, 200, r);
        }

        private void handleGetStats(HttpExchange exchange) throws Exception {
            List<Order> orders = orderService.getAllOrders();
            List<Product> products = productService.getAllProducts(true);
            List<Product> lowStock = productService.getLowStockProducts(15);
            List<User> customers = customerService.getAllCustomers();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Order o : orders) {
                if (o.getStatus() != OrderStatus.CANCELLED) {
                    totalRevenue = totalRevenue.add(o.getTotalAmount());
                }
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalRevenue", totalRevenue);
            stats.put("totalOrders", orders.size());
            stats.put("totalProducts", products.size());
            stats.put("totalCustomers", customers.size());
            stats.put("lowStockCount", lowStock.size());

            sendJsonResponse(exchange, 200, stats);
        }
    }

    // -------------------------------------------------------------
    // Static File Handler (Serves Web Frontend)
    // -------------------------------------------------------------
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Strip query params
            int queryIdx = path.indexOf('?');
            if (queryIdx >= 0) path = path.substring(0, queryIdx);

            // Look in web/ folder or resources/web/
            File file = new File("web" + path);
            if (!file.exists()) {
                file = new File("src/main/resources/web" + path);
            }

            if (!file.exists() || file.isDirectory()) {
                // Return index.html for SPA or 404
                File indexFile = new File("web/index.html");
                if (indexFile.exists()) {
                    file = indexFile;
                } else {
                    String msg = "404 Not Found: " + path;
                    exchange.sendResponseHeaders(404, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes(StandardCharsets.UTF_8));
                    }
                    return;
                }
            }

            String mime = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", mime);

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // -------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonHelper.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", true);
        err.put("message", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private static int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[3]);
    }

    private static String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
