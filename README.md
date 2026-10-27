# Enterprise E-Commerce Product Management System

An end-to-end, production-grade **E-Commerce and Product Management System** built with **Core Java**, **JDBC (Java Database Connectivity)**, and **MySQL**.

This system implements a clean **Layered Architecture** with the **Data Access Object (DAO) Pattern**, **Service Layer**, **Cryptographic Security**, and **ACID Transaction Management** with row-level locking for atomic checkouts and cancellations.

---

## 🌟 Key Features

### 🛒 1. Product Catalog & Inventory Management (CRUD)
- **Product CRUD**: Add, read, update, delete, and toggle status (Active/Inactive) of products.
- **Category Hierarchy**: Organize products by category with relational foreign key integrity.
- **Stock Tracking & Adjustments**: Atomic delta adjustments (`+` / `-`), absolute stock levels, and real-time inventory checks.
- **Low-Stock Alerts**: Instant reporting of products with inventory levels beneath user-configurable thresholds.
- **Search & Filtering**: Multi-parameter search by keyword, category, and minimum/maximum price bounds.

### 🛍️ 2. Customer Shopping Cart
- **Persistent Cart**: Automatically provisioned shopping cart linked to each customer account.
- **Live Stock Validations**: Verifies available quantities in real-time before adding or updating items.
- **Cart Calculations**: Subtotals per item line and dynamic cart grand total.
- **Item Adjustments**: Update item quantities, remove specific items, or clear cart.

### 📦 3. ACID Transactional Checkout & Orders
- **Atomic Transactions (`conn.setAutoCommit(false)`)**:
  - Row-level locking with `SELECT ... FOR UPDATE` to avoid race conditions.
  - Stock verification and atomic stock deduction (`UPDATE products SET stock_quantity = stock_quantity - ?`).
  - Order snapshot generation with frozen product details and prices.
  - Order items line insertion in batch.
  - Automatic shopping cart clearing.
  - Automatic `conn.rollback()` on any failure or insufficient inventory.
- **Order Cancellation with Inventory Restock**: Automatically returns purchased items back to active inventory using transactional rollback safety.
- **Order Tracking & Progression**: Status tracking (`PENDING` ➔ `PROCESSING` ➔ `SHIPPED` ➔ `DELIVERED` ➔ `CANCELLED`).
- **Professional ASCII Invoices**: Clean, printable invoices displaying order number, customer snapshot, items table, and totals.

### 👤 4. Customer & Account Management
- **Role-Based Access Control**: Differentiates between `ADMIN` and `CUSTOMER` roles.
- **Cryptographic Security**: Passwords hashed using **SHA-256** combined with a cryptographically secure random 16-byte salt (`PasswordUtil`).
- **Customer Directory**: Admin view of registered customers and their historical purchase summaries.
- **Profile Management**: Customers can update shipping addresses, phone numbers, and change passwords.

### 🛠️ 5. Auto-Initialization & Resilient Diagnostics
- **Automatic Schema & Seed Provisioning**: Detects missing database tables on first launch and automatically executes `schema.sql` and `seed_data.sql`.
- **Runtime Diagnostic Wizard**: Built-in interactive database tester and credential updater in case your MySQL server runs on custom credentials or ports.

---

## 🏗️ System Architecture

```
com.ecommerce/
├── App.java                      # Application Entry Point
├── SystemVerificationTest.java    # Offline Automated Verification Test Suite
├── model/                         # Domain Entities
│   ├── UserRole.java              # Enum: ADMIN, CUSTOMER
│   ├── User.java                  # Customer / Admin Entity
│   ├── Category.java              # Product Category
│   ├── Product.java               # Product & Stock Model
│   ├── Cart.java                  # Shopping Cart
│   ├── CartItem.java              # Cart Line Item
│   ├── OrderStatus.java           # Enum: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
│   ├── Order.java                 # Customer Order
│   └── OrderItem.java             # Order Snapshot Line Item
├── dao/                           # Data Access Object Interfaces
│   ├── UserDao.java
│   ├── CategoryDao.java
│   ├── ProductDao.java
│   ├── CartDao.java
│   ├── OrderDao.java
│   └── impl/                      # JDBC Implementations
│       ├── UserDaoImpl.java
│       ├── CategoryDaoImpl.java
│       ├── ProductDaoImpl.java
│       ├── CartDaoImpl.java
│       └── OrderDaoImpl.java      # Full ACID Transaction Support
├── service/                       # Business Logic Layer
│   ├── AuthService.java           # Authentication, Registration & Salt/Hashing
│   ├── CustomerService.java       # Customer Directory & Profile Management
│   ├── CategoryService.java       # Category Management
│   ├── ProductService.java        # Catalog Operations & Stock Alerts
│   ├── CartService.java           # Cart Validations & Stock Checking
│   └── OrderService.java          # Checkout Orchestration & Cancellation
├── util/                          # Helpers & Infrastructure
│   ├── DatabaseConnection.java    # Singleton JDBC Manager & Config Loader
│   ├── DatabaseInitializer.java   # Auto Table Creation & Seed Runner
│   ├── PasswordUtil.java          # SHA-256 + Salt Cryptographic Hasher
│   ├── ConsoleTable.java          # ASCII Terminal Table Formatter
│   ├── InputValidator.java        # Safe Console Input Reader with Validation
│   └── AnsiColor.java             # Terminal Colors & Styling
└── ui/                            # Presentation Layer
    ├── UserSession.java           # Active Session Holder
    ├── MainMenu.java              # Primary Navigation & Database Wizard
    ├── AdminMenu.java             # Administrator Control Panel
    └── CustomerMenu.java          # Customer Storefront & Cart Portal
```

---

## 🗄️ Database Schema & Relational Design

The system uses an **InnoDB** relational schema in MySQL with foreign key constraints, unique indexes, and cascading rules:

| Table | Primary Key | Foreign Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `users` | `id` | `UNIQUE(username)`, `UNIQUE(email)` | Stores customer and administrator accounts with password hashes and salts. |
| `categories` | `id` | `UNIQUE(name)` | Classifies products into departments. |
| `products` | `id` | `FK(category_id) -> categories(id)`, `UNIQUE(sku)` | Catalog items, active state, pricing, and live inventory. |
| `carts` | `id` | `FK(customer_id) -> users(id) ON DELETE CASCADE` | 1-to-1 active shopping cart per customer. |
| `cart_items` | `id` | `FK(cart_id)`, `FK(product_id)`, `UNIQUE(cart_id, product_id)` | Line items inside an active cart. |
| `orders` | `id` | `FK(customer_id) -> users(id)`, `UNIQUE(order_number)` | Placed purchase orders with shipping, payment, and status. |
| `order_items` | `id` | `FK(order_id) -> orders(id) ON DELETE CASCADE` | Frozen snapshots of purchased products, unit prices, and quantities. |

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java Development Kit (JDK)**: Java 17 or Java 21+.
- **MySQL Database Server**: MySQL 8.0+ running on `localhost:3306` (or configured via `db.properties`).

### 2. Configure Database Credentials
Edit `src/main/resources/db.properties` (or `db.properties` in root) to match your local MySQL configuration:

```properties
db.host=localhost
db.port=3306
db.name=ecommerce_db
db.username=root
db.password=your_mysql_password
db.url=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
```

> **Note**: You don't need to manually run SQL scripts in MySQL Workbench! When you launch the application, `DatabaseInitializer` will automatically create the `ecommerce_db` database, tables, and seed sample data if not already present.

### 3. Build & Run the System

#### 🌐 Launch Full-Stack Web Application (Recommended)
Launch the embedded Java HTTP Server and open the web application in your browser:
- **PowerShell**:
  ```powershell
  .\run-web.ps1
  ```
- **Windows Command Prompt**:
  ```cmd
  run-web.bat
  ```
- **Browser URL**: Open **`http://localhost:8080`**

#### 💻 Launch Interactive Terminal CLI Mode
- **PowerShell**:
  ```powershell
  .\run.ps1
  ```
- **Windows Command Prompt**:
  ```cmd
  run.bat
  ```

#### 📦 Using Maven
```bash
mvn clean compile
mvn exec:java
```

---

## 🔑 Default Seed Accounts

The initial database seed data provides pre-configured credentials:

| Role | Username | Password | Email | Full Name |
| :--- | :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | `admin@ecommerce.com` | System Administrator |
| **Customer** | `john_doe` | `user123` | `john@example.com` | John Doe |
| **Customer** | `jane_smith` | `user123` | `jane@example.com` | Jane Smith |

You can also use **Option 2** in the Main Menu to register a brand new customer account at any time.

---

## 🧪 Automated System Architecture Verification

To verify all core components, security hashing, cart recalculation math, table renderers, and database configs without requiring an active MySQL connection:

```powershell
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;src/main/resources" com.ecommerce.SystemVerificationTest
```

Expected output:
```text
==================================================
   E-COMMERCE SYSTEM ARCHITECTURE VERIFICATION
==================================================
Testing PasswordUtil (Salt + SHA-256)... [PASSED]
Testing Domain Models & Cart/Order Calculations... [PASSED]

Testing ConsoleTable Rendering Format:
+----+---------+-----------------+------------+-----+----------+
| ID | SKU     | Product Name    | Unit Price | Qty | Subtotal |
+----+---------+-----------------+------------+-----+----------+
| 1  | SKU-001 | MacBook Pro 14" | $1999.99   | 2   | $3999.98 |
| 2  | SKU-002 | Magic Mouse     | $99.50     | 1   | $99.50   |
+----+---------+-----------------+------------+-----+----------+
Table formatted successfully.
Testing Database Configuration Reader... [PASSED]

==================================================
   ALL SYSTEM VERIFICATION CHECKS PASSED [OK]
==================================================
```

---

## 📋 Design Patterns Applied

1. **DAO (Data Access Object) Pattern**: Decouples business logic from persistence logic. DAO interfaces (`ProductDao`, `OrderDao`, etc.) define contracts, while `*DaoImpl` classes encapsulate raw JDBC prepared statements and result set mapping.
2. **ACID Transaction Management**: Orders and order items are saved atomically with row-level locks (`SELECT ... FOR UPDATE`), ensuring inventory cannot become negative in concurrent scenarios. If any step fails, `conn.rollback()` restores state.
3. **Singleton Pattern**: `DatabaseConnection` manages centralized database access and configuration properties.
4. **Service Layer Pattern**: Business validation (e.g., password criteria, stock thresholds, transition states) is cleanly contained inside services (`ProductService`, `CartService`, `OrderService`).
5. **State & Session Pattern**: `UserSession` encapsulates authenticated user context across menu navigations.
