package com.ecommerce.web;

import com.ecommerce.model.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Lightweight JSON utility for serialization and deserialization without third-party dependencies.
 */
public class JsonHelper {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Enum<?>) {
            return "\"" + ((Enum<?>) obj).name() + "\"";
        }
        if (obj instanceof Collection<?>) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map<?, ?>) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        // Domain models
        if (obj instanceof Product p) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("categoryId", p.getCategoryId());
            map.put("categoryName", p.getCategoryName());
            map.put("sku", p.getSku());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("price", p.getPrice());
            map.put("stockQuantity", p.getStockQuantity());
            map.put("active", p.isActive());
            map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            if (p instanceof Apparel a) {
                map.put("isApparel", true);
                map.put("size", a.getSize());
                map.put("brand", a.getBrand());
                map.put("material", a.getMaterial());
                map.put("gender", a.getGender());
                map.put("color", a.getColor());
            }
            return toJson(map);
        }
        if (obj instanceof Category c) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("description", c.getDescription());
            return toJson(map);
        }
        if (obj instanceof User u) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("address", u.getAddress());
            map.put("role", u.getRole().name());
            return toJson(map);
        }
        if (obj instanceof Cart c) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("customerId", c.getCustomerId());
            map.put("items", c.getItems());
            map.put("totalAmount", c.getTotalAmount());
            map.put("totalItemCount", c.getTotalItemCount());
            return toJson(map);
        }
        if (obj instanceof CartItem ci) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ci.getId());
            map.put("cartId", ci.getCartId());
            map.put("productId", ci.getProductId());
            map.put("productSku", ci.getProductSku());
            map.put("productName", ci.getProductName());
            map.put("unitPrice", ci.getUnitPrice());
            map.put("stockAvailable", ci.getStockAvailable());
            map.put("quantity", ci.getQuantity());
            map.put("subtotal", ci.getSubtotal());
            return toJson(map);
        }
        if (obj instanceof Order o) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", o.getId());
            map.put("orderNumber", o.getOrderNumber());
            map.put("customerId", o.getCustomerId());
            map.put("customerName", o.getCustomerName());
            map.put("customerEmail", o.getCustomerEmail());
            map.put("totalAmount", o.getTotalAmount());
            map.put("status", o.getStatus().name());
            map.put("shippingAddress", o.getShippingAddress());
            map.put("paymentMethod", o.getPaymentMethod());
            map.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
            map.put("items", o.getItems());
            return toJson(map);
        }
        if (obj instanceof OrderItem oi) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", oi.getId());
            map.put("orderId", oi.getOrderId());
            map.put("productId", oi.getProductId());
            map.put("productName", oi.getProductName());
            map.put("productSku", oi.getProductSku());
            map.put("unitPrice", oi.getUnitPrice());
            map.put("quantity", oi.getQuantity());
            map.put("subtotal", oi.getSubtotal());
            return toJson(map);
        }

        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return map;

        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return map;

        List<String> pairs = splitTopLevel(inner, ',');
        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = unescapeString(pair.substring(0, colonIdx).trim());
                String valStr = pair.substring(colonIdx + 1).trim();
                map.put(key, parseJsonValue(valStr));
            }
        }
        return map;
    }

    private static Object parseJsonValue(String val) {
        if (val.equalsIgnoreCase("null")) return null;
        if (val.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (val.equalsIgnoreCase("false")) return Boolean.FALSE;
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            return unescapeString(val);
        }
        try {
            if (val.contains(".")) {
                return new BigDecimal(val);
            }
            return Integer.parseInt(val);
        } catch (NumberFormatException ignored) {}
        return val;
    }

    private static List<String> splitTopLevel(String str, char delimiter) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == delimiter && depth == 0) {
                    list.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(c);
        }
        if (current.length() > 0) {
            list.add(current.toString().trim());
        }
        return list;
    }

    private static String unescapeString(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
