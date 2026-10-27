-- ====================================================================
-- E-Commerce Product Management System
-- Initial Seed Data (Categories, Products, Users)
-- ====================================================================

USE ecommerce_db;

-- 1. Insert Initial Users
-- Password for 'admin': admin123
-- Password for 'john_doe': user123
INSERT INTO users (username, password_hash, salt, full_name, email, phone, address, role) VALUES
('admin', '2e9dd043ed9ef0fa81624a4ac96fc38d7794731a7f0ea48c510964a288791818', 'abcdef1234567890', 'System Administrator', 'admin@ecommerce.com', '+1-555-0100', '100 Tech Park, Silicon Valley, CA', 'ADMIN'),
('john_doe', '775ed0cb26eec0cf3e99b428066a9fefe9091015c25c55a8fdbadbe67b0b8def', 'abcdef1234567890', 'John Doe', 'john@example.com', '+1-555-0199', '742 Evergreen Terrace, Springfield, OR', 'CUSTOMER'),
('jane_smith', '775ed0cb26eec0cf3e99b428066a9fefe9091015c25c55a8fdbadbe67b0b8def', 'abcdef1234567890', 'Jane Smith', 'jane@example.com', '+1-555-0255', '124 Conch Street, Bikini Bottom, HI', 'CUSTOMER')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Insert Categories (Category 3 is 'Clothes')
INSERT INTO categories (id, name, description) VALUES
(1, 'Electronics', 'Smartphones, laptops, tablets, and electronic accessories'),
(2, 'Footwear', 'Athletic shoes, casual sneakers, boots, and formal footwear'),
(3, 'Clothes', 'Men and Women branded fashion apparel, jackets, shirts, jeans, and premium clothing'),
(4, 'Home & Kitchen', 'Modern appliances, cookware, furniture, and home decors'),
(5, 'Books & Media', 'Paperbacks, hardcovers, journals, and educational publications')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 3. Insert Products (Category 3 'Clothes' contains 20 items)
INSERT INTO products (id, category_id, sku, name, description, price, stock_quantity, is_active) VALUES
(1, 1, 'ELEC-MBP-14', 'MacBook Pro 14"', 'Apple M3 Pro chip, 18GB Unified Memory, 512GB SSD storage', 1999.99, 15, TRUE),
(2, 1, 'ELEC-IPH-15P', 'iPhone 15 Pro', 'A17 Pro chip, Titanium design, 48MP camera, 256GB storage', 1099.00, 25, TRUE),
(3, 1, 'ELEC-SNY-WH', 'Sony WH-1000XM5 Headphones', 'Industry-leading noise cancelling wireless headphones with 30-hour battery', 349.99, 40, TRUE),
(4, 1, 'ELEC-LOG-MX', 'Logitech MX Master 3S Mouse', 'Wireless performance mouse with ultra-fast scrolling and ergonomic grip', 99.99, 50, TRUE),
(5, 2, 'FOOT-NK-AF1', 'Nike Air Force 1 07', 'Legendary basketball icon featuring stitched overlays and clean leather finish', 115.00, 30, TRUE),
(6, 2, 'FOOT-AD-UB23', 'Adidas Ultraboost Light', 'Responsive Boost midsole cushioning for supreme daily running comfort', 189.95, 20, TRUE),
(7, 3, 'APP-PAT-DNM', 'Levi’s 511 Slim Jeans', 'Classic modern slim-fit denim jeans with room to move', 69.50, 45, TRUE),
(8, 3, 'APP-TH-JCKT', 'Tommy Hilfiger Bomber Jacket', 'Water-resistant vintage lightweight zip-up bomber jacket', 129.99, 18, TRUE),
(9, 4, 'HOME-NS-COFF', 'Nespresso VertuoPlus Coffee Maker', 'Single-serve coffee and espresso machine with precision brewing technology', 169.00, 12, TRUE),
(10, 4, 'HOME-DY-V15', 'Dyson V15 Detect Vacuum', 'Powerful cordless vacuum cleaner with laser illumination technology', 649.99, 8, TRUE),
(11, 5, 'BK-CLEAN-CODE', 'Clean Code by Robert C. Martin', 'A Handbook of Agile Software Craftsmanship and object-oriented design', 42.50, 60, TRUE),
(12, 5, 'BK-DESIGN-PTRN', 'Design Patterns (Gang of Four)', 'Elements of Reusable Object-Oriented Software architectural reference', 54.00, 35, TRUE),
(13, 3, 'CLOTH-OXF-SHT', 'Classic Oxford Cotton Shirt', 'Tailored fit button-down oxford shirt crafted from 100% breathable Egyptian cotton', 54.99, 35, TRUE),
(14, 3, 'CLOTH-WOOL-SWT', 'Merino Wool Crewneck Sweater', 'Ultra-soft premium lightweight merino wool knit sweater for seasonal layering', 89.00, 28, TRUE),
(15, 3, 'CLOTH-STR-HD', 'Streetwear Oversized Hoodie', 'Heavyweight 450 GSM fleece hoodie with dropped shoulders and double-lined hood', 65.00, 42, TRUE),
(16, 3, 'CLOTH-SLM-CHN', 'Slim-Fit Stretch Chino Pants', 'Versatile four-way stretch cotton twill chinos suitable for work and casual wear', 58.50, 50, TRUE),
(17, 3, 'CLOTH-DNM-JCKT', 'Vintage Denim Trucker Jacket', 'Classic 90s vintage wash distressed denim trucker jacket with metal hardware', 98.00, 22, TRUE),
(18, 3, 'CLOTH-TLR-BLZ', 'Tailored Modern Slim Blazer', 'Single-breasted structured blazer crafted from Italian wool-blend fabric', 185.00, 15, TRUE),
(19, 3, 'CLOTH-LIN-SHT', 'Pure Linen Summer Shirt', 'Relaxed breathable French linen long-sleeve shirt in clean airy silhouette', 49.99, 30, TRUE),
(20, 3, 'CLOTH-LTH-JKT', 'Classic Leather Moto Jacket', 'Handcrafted genuine full-grain leather motorcycle jacket with asymmetrical zip', 249.99, 10, TRUE),
(21, 3, 'CLOTH-CRG-PNT', 'Relaxed Fit Utility Cargo Pants', 'Durable ripstop cotton cargo pants with multi-functional flap pockets', 64.00, 38, TRUE),
(22, 3, 'CLOTH-PL-POLO', 'Pima Cotton Pique Polo', 'Premium combed Peruvian Pima cotton polo shirt with mother-of-pearl buttons', 45.00, 40, TRUE),
(23, 3, 'CLOTH-WNT-PRK', 'Sub-Zero Down Winter Parka', 'Waterproof insulated down parka with faux-fur trimmed hood and thermal storm cuffs', 220.00, 14, TRUE),
(24, 3, 'CLOTH-CSH-CDG', 'Cashmere Blend Knit Cardigan', 'Luxurious plush cashmere cardigan with ribbed cuffs and tortoiseshell buttons', 110.00, 20, TRUE),
(25, 3, 'CLOTH-GRP-TEE', 'Heavyweight Vintage Graphic Tee', 'Boxy-fit retro graphic t-shirt made from pre-shrunk organic ring-spun cotton', 34.50, 60, TRUE),
(26, 3, 'CLOTH-FLR-DRS', 'Bohemian Floral Midi Dress', 'Flowing A-line floral printed midi dress with tiered ruffle hem and puff sleeves', 79.99, 25, TRUE),
(27, 3, 'CLOTH-ATH-JOG', 'Performance Tech Fleece Joggers', 'Athletic tapered sweatpants featuring zip stash pocket and ergonomic articulation', 52.00, 45, TRUE),
(28, 3, 'CLOTH-CRD-OVR', 'Corduroy Relaxed Overshirt', 'Thick ribbed velvety corduroy overshirt jacket perfect for light autumn weather', 74.00, 32, TRUE),
(29, 3, 'CLOTH-TRN-COT', 'Classic Double-Breasted Trench Coat', 'Timeless British water-resistant gabardine trench coat with storm flap and belt', 195.00, 16, TRUE),
(30, 3, 'CLOTH-RBD-TRT', 'Ribbed Wool Turtleneck Sweater', 'Fitted fine-gauge rib knit turtleneck sweater in minimalist charcoal tone', 68.00, 30, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_quantity=VALUES(stock_quantity);
