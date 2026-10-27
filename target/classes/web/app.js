/**
 * NexCommerce Frontend Application Controller
 * Handles REST API synchronization, state management, cart drawer, and admin dashboard.
 */

// Application State
const state = {
    currentUser: {
        id: 2,
        username: 'john_doe',
        fullName: 'John Doe',
        email: 'john@example.com',
        role: 'CUSTOMER'
    },
    categories: [],
    products: [],
    cart: {
        items: [],
        totalAmount: 0,
        totalItemCount: 0
    },
    orders: [],
    stats: {},
    activeView: 'store', // 'store', 'admin', 'customerOrders'
    activeAdminTab: 'products',
    filters: {
        category: 'all',
        keyword: '',
        priceRange: 'all',
        sort: 'popular'
    }
};

// Curated high-resolution imagery for products
const productImages = {
    // Electronics
    'ELEC-MBP-14': 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80',
    'ELEC-IPH-15P': 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=600&q=80',
    'ELEC-SNY-WH': 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80',
    'ELEC-LOG-MX': 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=600&q=80',
    // Footwear
    'FOOT-NK-AF1': 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=600&q=80',
    'FOOT-AD-UB23': 'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?auto=format&fit=crop&w=600&q=80',
    // Clothes (20 items with original fashion photography)
    'APP-PAT-DNM': 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=600&q=80',
    'APP-TH-JCKT': 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80',
    'CLOTH-OXF-SHT': 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=600&q=80',
    'CLOTH-WOOL-SWT': 'https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?auto=format&fit=crop&w=600&q=80',
    'CLOTH-STR-HD': 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=600&q=80',
    'CLOTH-SLM-CHN': 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=600&q=80',
    'CLOTH-DNM-JCKT': 'https://images.unsplash.com/photo-1576871337622-98d48d1cf531?auto=format&fit=crop&w=600&q=80',
    'CLOTH-TLR-BLZ': 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=600&q=80',
    'CLOTH-LIN-SHT': 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=600&q=80',
    'CLOTH-LTH-JKT': 'https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=600&q=80',
    'CLOTH-CRG-PNT': 'https://images.unsplash.com/photo-1517445312882-bc9910d016b7?auto=format&fit=crop&w=600&q=80',
    'CLOTH-PL-POLO': 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?auto=format&fit=crop&w=600&q=80',
    'CLOTH-WNT-PRK': 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=600&q=80',
    'CLOTH-CSH-CDG': 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?auto=format&fit=crop&w=600&q=80',
    'CLOTH-GRP-TEE': 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80',
    'CLOTH-FLR-DRS': 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=600&q=80',
    'CLOTH-ATH-JOG': 'https://images.unsplash.com/photo-1552902865-b72c031ac5ea?auto=format&fit=crop&w=600&q=80',
    'CLOTH-CRD-OVR': 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=600&q=80',
    'CLOTH-TRN-COT': 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80',
    'CLOTH-RBD-TRT': 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80',
    // Home & Kitchen
    'HOME-NS-COFF': 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=600&q=80',
    'HOME-DY-V15': 'https://images.unsplash.com/photo-1558317374-067fb5f30001?auto=format&fit=crop&w=600&q=80',
    // Books & Media
    'BK-CLEAN-CODE': 'https://images.unsplash.com/photo-1532012164546-f432f2e3777f?auto=format&fit=crop&w=600&q=80',
    'BK-DESIGN-PTRN': 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'
};

// Automatically resolve backend URL whether accessed via http://localhost:8080, custom port, or file://
const API_BASE = (window.location.protocol === 'file:' || (window.location.port && window.location.port !== '8080'))
    ? 'http://localhost:8080/api'
    : '/api';

// ====================================================================
// INITIALIZATION
// ====================================================================
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    checkBackendHealth();
    loadCategories();
    loadProducts();
    loadCart();
});

async function checkBackendHealth() {
    const badge = document.getElementById('backendStatusBadge');
    const text = document.getElementById('backendStatusText');
    const dot = badge ? badge.querySelector('.status-dot') : null;
    try {
        const res = await fetch(`${API_BASE}/categories`);
        if (res.ok) {
            if (text) text.textContent = 'Backend Connected';
            if (badge) {
                badge.style.background = 'rgba(16, 185, 129, 0.15)';
                badge.style.color = '#10b981';
                badge.style.borderColor = 'rgba(16, 185, 129, 0.35)';
            }
            if (dot) {
                dot.style.background = '#10b981';
                dot.style.boxShadow = '0 0 8px #10b981';
            }
        } else {
            throw new Error('Non-200 response');
        }
    } catch (e) {
        if (text) text.textContent = 'Backend Offline';
        if (badge) {
            badge.style.background = 'rgba(239, 68, 68, 0.15)';
            badge.style.color = '#ef4444';
            badge.style.borderColor = 'rgba(239, 68, 68, 0.35)';
        }
        if (dot) {
            dot.style.background = '#ef4444';
            dot.style.boxShadow = '0 0 8px #ef4444';
        }
    }
}

// Setup All Interactive Events
function setupEventListeners() {
    // Portal Switcher
    document.getElementById('viewStoreBtn').addEventListener('click', () => switchView('store'));
    document.getElementById('viewAdminBtn').addEventListener('click', () => {
        if (state.currentUser.role !== 'ADMIN') {
            showToast('Switching to Admin profile for demonstration...', 'info');
            state.currentUser = {
                id: 1,
                username: 'admin',
                fullName: 'System Administrator',
                email: 'admin@ecommerce.com',
                role: 'ADMIN'
            };
            updateUserUI();
        }
        switchView('admin');
        loadAdminData();
    });

    // Back to store from customer orders
    document.getElementById('backToStoreBtn').addEventListener('click', () => switchView('store'));

    // Cart Drawer Controls
    const cartDrawer = document.getElementById('cartDrawer');
    const backdrop = document.getElementById('cartDrawerBackdrop');
    document.getElementById('cartTriggerBtn').addEventListener('click', () => {
        cartDrawer.classList.add('open');
        backdrop.classList.add('open');
    });
    const closeCart = () => {
        cartDrawer.classList.remove('open');
        backdrop.classList.remove('open');
    };
    document.getElementById('closeCartDrawerBtn').addEventListener('click', closeCart);
    backdrop.addEventListener('click', closeCart);

    // Global Search with debounce
    let searchTimeout;
    const searchInput = document.getElementById('globalSearchInput');
    const clearBtn = document.getElementById('clearSearchBtn');
    searchInput.addEventListener('input', (e) => {
        const val = e.target.value.trim();
        clearBtn.style.display = val.length > 0 ? 'block' : 'none';
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            state.filters.keyword = val;
            applyFilters();
        }, 250);
    });
    clearBtn.addEventListener('click', () => {
        searchInput.value = '';
        clearBtn.style.display = 'none';
        state.filters.keyword = '';
        applyFilters();
    });

    // Filter & Sort dropdowns
    document.getElementById('priceFilterSelect').addEventListener('change', (e) => {
        state.filters.priceRange = e.target.value;
        applyFilters();
    });
    document.getElementById('sortSelect').addEventListener('change', (e) => {
        state.filters.sort = e.target.value;
        applyFilters();
    });
    document.getElementById('resetFiltersBtn').addEventListener('click', () => {
        searchInput.value = '';
        state.filters.keyword = '';
        state.filters.category = 'all';
        state.filters.priceRange = 'all';
        state.filters.sort = 'popular';
        document.getElementById('priceFilterSelect').value = 'all';
        document.getElementById('sortSelect').value = 'popular';
        document.querySelectorAll('.category-chip').forEach(c => c.classList.remove('active'));
        document.querySelector('.category-chip[data-id="all"]')?.classList.add('active');
        applyFilters();
    });

    // User Menu Dropdown
    const userMenuBtn = document.getElementById('userMenuBtn');
    const userDropdown = document.getElementById('userDropdown');
    userMenuBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        userDropdown.classList.toggle('open');
    });
    document.addEventListener('click', () => userDropdown.classList.remove('open'));

    // Dropdown items
    document.getElementById('dropdownOrdersBtn').addEventListener('click', () => {
        userDropdown.classList.remove('open');
        switchView('customerOrders');
        loadCustomerOrders();
    });
    document.getElementById('dropdownSwitchUserBtn').addEventListener('click', () => {
        userDropdown.classList.remove('open');
        document.getElementById('authModal').classList.add('open');
    });
    document.getElementById('closeAuthModalBtn').addEventListener('click', () => {
        document.getElementById('authModal').classList.remove('open');
    });
    document.getElementById('quickLoginAdminBtn').addEventListener('click', () => {
        state.currentUser = {
            id: 1,
            username: 'admin',
            fullName: 'System Administrator',
            email: 'admin@ecommerce.com',
            role: 'ADMIN'
        };
        updateUserUI();
        document.getElementById('authModal').classList.remove('open');
        showToast('Logged in as Administrator', 'success');
        switchView('admin');
        loadAdminData();
    });
    document.getElementById('quickLoginCustomerBtn').addEventListener('click', () => {
        state.currentUser = {
            id: 2,
            username: 'john_doe',
            fullName: 'John Doe',
            email: 'john@example.com',
            role: 'CUSTOMER'
        };
        updateUserUI();
        document.getElementById('authModal').classList.remove('open');
        showToast('Logged in as Customer (John Doe)', 'success');
        switchView('store');
        loadCart();
    });
    document.getElementById('dropdownLogoutBtn').addEventListener('click', () => {
        userDropdown.classList.remove('open');
        showToast('Session reset to Customer Demo mode.', 'info');
    });

    // Cart actions
    document.getElementById('clearCartBtn').addEventListener('click', clearCart);
    document.getElementById('proceedToCheckoutBtn').addEventListener('click', () => {
        if (!state.cart.items || state.cart.items.length === 0) {
            showToast('Your cart is empty!', 'warning');
            return;
        }
        closeCart();
        openCheckoutModal();
    });

    // Checkout Modal
    document.getElementById('closeCheckoutModalBtn').addEventListener('click', () => {
        document.getElementById('checkoutModal').classList.remove('open');
    });
    document.querySelectorAll('.payment-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('.payment-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            card.querySelector('input').checked = true;
        });
    });
    document.getElementById('submitOrderBtn').addEventListener('click', submitOrder);

    // Receipt Modal
    document.getElementById('closeReceiptModalBtn').addEventListener('click', () => {
        document.getElementById('receiptModal').classList.remove('open');
    });
    document.getElementById('receiptDoneBtn').addEventListener('click', () => {
        document.getElementById('receiptModal').classList.remove('open');
        switchView('store');
    });

    // Admin Tabs
    document.querySelectorAll('.admin-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.admin-tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.admin-tab-pane').forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            const tab = btn.dataset.tab;
            state.activeAdminTab = tab;
            document.getElementById(`admin${capitalize(tab)}Pane`).classList.add('active');
        });
    });

    // Admin Add Product Modal
    document.getElementById('openAddProductModalBtn').addEventListener('click', () => openProductModal());
    document.getElementById('closeProductModalBtn').addEventListener('click', () => {
        document.getElementById('productModal').classList.remove('open');
    });
    document.getElementById('cancelProductModalBtn').addEventListener('click', () => {
        document.getElementById('productModal').classList.remove('open');
    });
    document.getElementById('productForm').addEventListener('submit', saveProduct);

    // Admin Add Category Modal
    const openAddCategory = () => openCategoryModal();
    document.getElementById('openAddCategoryBtn')?.addEventListener('click', openAddCategory);
    document.getElementById('openAddCategoryTopBtn')?.addEventListener('click', openAddCategory);
    document.getElementById('closeCategoryModalBtn')?.addEventListener('click', closeCategoryModal);
    document.getElementById('cancelCategoryModalBtn')?.addEventListener('click', closeCategoryModal);
    document.getElementById('categoryForm')?.addEventListener('submit', saveCategory);

    // Admin Refresh
    document.getElementById('adminRefreshBtn').addEventListener('click', () => {
        loadAdminData();
        showToast('Dashboard metrics and records refreshed', 'info');
    });
}

function updateUserUI() {
    const user = state.currentUser;
    document.getElementById('userNameLabel').textContent = user.fullName;
    document.getElementById('userRoleLabel').textContent = user.role === 'ADMIN' ? 'Administrator' : 'Customer';
    document.getElementById('userAvatar').textContent = user.fullName.charAt(0);
    document.getElementById('dropdownFullName').textContent = user.fullName;
    document.getElementById('dropdownEmail').textContent = user.email;
    if (user.role === 'ADMIN') {
        document.getElementById('userAvatar').classList.add('admin-avatar');
    } else {
        document.getElementById('userAvatar').classList.remove('admin-avatar');
    }
}

function switchView(viewName) {
    state.activeView = viewName;
    document.querySelectorAll('.view-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.segmented-btn').forEach(b => b.classList.remove('active'));

    if (viewName === 'store') {
        document.getElementById('storefrontView').classList.add('active');
        document.getElementById('viewStoreBtn').classList.add('active');
    } else if (viewName === 'admin') {
        document.getElementById('adminView').classList.add('active');
        document.getElementById('viewAdminBtn').classList.add('active');
    } else if (viewName === 'customerOrders') {
        document.getElementById('customerOrdersView').classList.add('active');
    }
}

// ====================================================================
// DATA FETCHING & API SERVICES
// ====================================================================

async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        if (res.ok) {
            state.categories = await res.json();
            renderCategories();
            populateCategorySelects();
        }
    } catch (e) {
        console.warn('API unavailable; loading local categories.');
    }
}

async function loadProducts() {
    try {
        const res = await fetch(`${API_BASE}/products`);
        if (res.ok) {
            state.products = await res.json();
            applyFilters();
            updateHeroStats();
        }
    } catch (e) {
        console.warn('API unavailable; using cached products.');
    }
}

async function loadCart() {
    try {
        const res = await fetch(`${API_BASE}/cart?customerId=${state.currentUser.id}`);
        if (res.ok) {
            state.cart = await res.json();
            renderCart();
        }
    } catch (e) {
        console.warn('Cart API error.');
    }
}

async function loadAdminData() {
    try {
        // Fetch stats
        const statsRes = await fetch(`${API_BASE}/stats`);
        if (statsRes.ok) {
            state.stats = await statsRes.json();
            renderAdminStats();
        }

        // Fetch products (including inactive)
        const prodRes = await fetch(`${API_BASE}/products?all=true`);
        if (prodRes.ok) {
            state.adminProducts = await prodRes.json();
            renderAdminProductsTable();
        }

        // Fetch orders
        const ordRes = await fetch(`${API_BASE}/orders`);
        if (ordRes.ok) {
            state.adminOrders = await ordRes.json();
            renderAdminOrdersTable();
        }

        // Fetch customers
        const custRes = await fetch(`${API_BASE}/customers`);
        if (custRes.ok) {
            state.adminCustomers = await custRes.json();
            renderAdminCustomersTable();
        }

        renderAdminCategoriesTable();
    } catch (e) {
        console.warn('Admin API error:', e);
    }
}

async function loadCustomerOrders() {
    try {
        const res = await fetch(`${API_BASE}/orders?customerId=${state.currentUser.id}`);
        if (res.ok) {
            state.customerOrders = await res.json();
            renderCustomerOrders();
        }
    } catch (e) {
        console.warn('Customer orders fetch error:', e);
    }
}

// ====================================================================
// STOREFRONT RENDERING
// ====================================================================

function renderCategories() {
    const container = document.getElementById('categoriesPillContainer');
    container.innerHTML = `<button class="category-chip active" data-id="all">All Departments</button>`;

    state.categories.forEach(cat => {
        const btn = document.createElement('button');
        btn.className = 'category-chip';
        btn.dataset.id = cat.id;
        btn.textContent = cat.name;
        btn.addEventListener('click', () => {
            document.querySelectorAll('.category-chip').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            state.filters.category = cat.id;
            applyFilters();
        });
        container.appendChild(btn);
    });

    container.querySelector('[data-id="all"]').addEventListener('click', (e) => {
        document.querySelectorAll('.category-chip').forEach(c => c.classList.remove('active'));
        e.target.classList.add('active');
        state.filters.category = 'all';
        applyFilters();
    });
}

function populateCategorySelects() {
    const select = document.getElementById('formProductCategory');
    select.innerHTML = '<option value="">-- Choose Category --</option>';
    state.categories.forEach(c => {
        select.innerHTML += `<option value="${c.id}">${c.name}</option>`;
    });
}

function applyFilters() {
    let filtered = [...state.products];

    // Filter by Category
    if (state.filters.category !== 'all') {
        const catId = parseInt(state.filters.category);
        filtered = filtered.filter(p => p.categoryId === catId);
    }

    // Filter by Keyword
    if (state.filters.keyword) {
        const q = state.filters.keyword.toLowerCase();
        filtered = filtered.filter(p =>
            p.name.toLowerCase().includes(q) ||
            p.description.toLowerCase().includes(q) ||
            p.sku.toLowerCase().includes(q) ||
            (p.categoryName && p.categoryName.toLowerCase().includes(q))
        );
    }

    // Filter by Price
    if (state.filters.priceRange !== 'all') {
        const [min, max] = state.filters.priceRange.split('-').map(Number);
        filtered = filtered.filter(p => p.price >= min && p.price <= max);
    }

    // Sort
    if (state.filters.sort === 'price-asc') {
        filtered.sort((a, b) => a.price - b.price);
    } else if (state.filters.sort === 'price-desc') {
        filtered.sort((a, b) => b.price - a.price);
    } else if (state.filters.sort === 'name-asc') {
        filtered.sort((a, b) => a.name.localeCompare(b.name));
    }

    renderProducts(filtered);
}

function renderProducts(items) {
    const grid = document.getElementById('productsGrid');
    const empty = document.getElementById('productsEmptyState');

    if (!items || items.length === 0) {
        grid.innerHTML = '';
        empty.classList.remove('hidden');
        return;
    }

    empty.classList.add('hidden');
    grid.innerHTML = items.map(p => {
        const imgUrl = productImages[p.sku] || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80';
        const inStock = p.stockQuantity > 0;
        let badgeClass = 'badge-in-stock';
        let badgeText = `${p.stockQuantity} in stock`;

        if (!inStock) {
            badgeClass = 'badge-out-of-stock';
            badgeText = 'Out of Stock';
        } else if (p.stockQuantity <= 10) {
            badgeClass = 'badge-low-stock';
            badgeText = `Low stock: ${p.stockQuantity}`;
        }

        const isClothes = p.isApparel || p.categoryName === 'Clothes' || p.categoryName === 'Apparel' || (p.sku && (p.sku.startsWith('APP-') || p.sku.startsWith('CLOTH-')));
        let apparelHtml = '';
        if (isClothes) {
            let brand = 'Premium Collection';
            if (p.name.includes("Levi")) brand = "Levi's";
            else if (p.name.includes("Tommy Hilfiger")) brand = "Tommy Hilfiger";
            else if (p.name.includes("Oxford") || p.name.includes("Shirt")) brand = "Oxford Studio";
            else if (p.name.includes("Wool") || p.name.includes("Sweater") || p.name.includes("Cardigan") || p.name.includes("Turtleneck")) brand = "Merino Knitwear";
            else if (p.name.includes("Hoodie") || p.name.includes("Graphic") || p.name.includes("Tee")) brand = "Urban Heritage";
            else if (p.name.includes("Chino") || p.name.includes("Cargo")) brand = "Tailored Fit";
            else if (p.name.includes("Blazer") || p.name.includes("Trench")) brand = "Sartorial Milan";
            else if (p.name.includes("Leather")) brand = "Atelier Leather";

            let size = "M / L";
            if (p.sku.includes("DNM") || p.sku.includes("CHN") || p.sku.includes("CRG") || p.sku.includes("JOG")) size = "32 / 34";
            else if (p.sku.includes("DRS")) size = "S / M";

            let material = "100% Cotton";
            if (p.name.includes("Denim")) material = "Classic Denim";
            else if (p.name.includes("Wool") || p.name.includes("Cashmere")) material = "Pure Wool Blend";
            else if (p.name.includes("Linen")) material = "French Linen";
            else if (p.name.includes("Leather")) material = "Full-Grain Leather";
            else if (p.name.includes("Fleece") || p.name.includes("Hoodie") || p.name.includes("Jogger")) material = "Heavyweight Fleece";
            else if (p.name.includes("Parka") || p.name.includes("Trench")) material = "Weatherproof";

            apparelHtml = `
                <div class="apparel-meta-tags" style="display: flex; gap: 6px; flex-wrap: wrap; margin: 6px 0 10px;">
                    <span style="font-size: 0.73rem; font-weight: 600; padding: 2px 7px; border-radius: 4px; background: rgba(255, 159, 0, 0.15); color: #ff9f00; border: 1px solid rgba(255, 159, 0, 0.3);">🏷️ ${escapeHtml(brand)}</span>
                    <span style="font-size: 0.73rem; font-weight: 500; padding: 2px 7px; border-radius: 4px; background: rgba(99, 102, 241, 0.15); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.3);">Size: ${escapeHtml(size)}</span>
                    <span style="font-size: 0.73rem; font-weight: 500; padding: 2px 7px; border-radius: 4px; background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3);">${escapeHtml(material)}</span>
                </div>
            `;
        }

        return `
            <div class="product-card" data-id="${p.id}">
                <div class="product-card-thumb">
                    <img src="${imgUrl}" alt="${escapeHtml(p.name)}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'">
                    <span class="product-card-badge ${badgeClass}">${badgeText}</span>
                </div>
                <div class="product-card-body">
                    <span class="product-card-category">${escapeHtml(p.categoryName || 'Item')} &bull; ${escapeHtml(p.sku)}</span>
                    <h3 class="product-card-title">${escapeHtml(p.name)}</h3>
                    <p class="product-card-desc">${escapeHtml(p.description)}</p>
                    ${apparelHtml}
                    <div class="product-card-footer">
                        <span class="product-card-price">$${parseFloat(p.price).toFixed(2)}</span>
                        <button class="add-cart-btn" onclick="addToCart(${p.id})" ${!inStock ? 'disabled' : ''}>
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                            ${inStock ? 'Add to Cart' : 'Sold Out'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function updateHeroStats() {
    document.getElementById('heroProductCount').textContent = state.products.length;
    document.getElementById('heroCategoryCount').textContent = state.categories.length;
}

// ====================================================================
// SHOPPING CART OPERATIONS
// ====================================================================

async function addToCart(productId) {
    try {
        const res = await fetch(`${API_BASE}/cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: state.currentUser.id,
                productId: productId,
                quantity: 1
            })
        });

        if (res.ok) {
            state.cart = await res.json();
            renderCart();
            showToast('Added to your shopping cart!', 'success');
        } else {
            const err = await res.json();
            showToast(err.message || 'Cannot add item to cart', 'error');
        }
    } catch (e) {
        showToast('Cart service unreachable', 'error');
    }
}

async function updateCartQuantity(productId, newQty) {
    try {
        const res = await fetch(`${API_BASE}/cart/update`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: state.currentUser.id,
                productId: productId,
                quantity: newQty
            })
        });

        if (res.ok) {
            state.cart = await res.json();
            renderCart();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (e) {
        showToast('Error updating quantity', 'error');
    }
}

async function removeCartItem(productId) {
    try {
        const res = await fetch(`${API_BASE}/cart/remove`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: state.currentUser.id,
                productId: productId
            })
        });

        if (res.ok) {
            state.cart = await res.json();
            renderCart();
            showToast('Item removed from cart', 'info');
        }
    } catch (e) {
        showToast('Failed to remove item', 'error');
    }
}

async function clearCart() {
    try {
        const res = await fetch(`${API_BASE}/cart/clear`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ customerId: state.currentUser.id })
        });
        if (res.ok) {
            state.cart = await res.json();
            renderCart();
            showToast('Cart cleared', 'info');
        }
    } catch (e) {
        showToast('Error clearing cart', 'error');
    }
}

function renderCart() {
    const container = document.getElementById('cartItemsContainer');
    const badge = document.getElementById('cartCountBadge');
    const itemsBadge = document.getElementById('cartItemsBadge');
    const subtotalEl = document.getElementById('cartSubtotalAmount');
    const taxEl = document.getElementById('cartTaxAmount');
    const totalEl = document.getElementById('cartTotalAmount');
    const progressBar = document.getElementById('shippingProgressBar');
    const progressText = document.getElementById('shippingProgressText');

    const items = state.cart.items || [];
    const count = state.cart.totalItemCount || 0;
    const subtotal = parseFloat(state.cart.totalAmount || 0);

    badge.textContent = count;
    itemsBadge.textContent = `${count} ${count === 1 ? 'item' : 'items'}`;

    // Free shipping goal ($100)
    const threshold = 100;
    const progress = Math.min(100, Math.round((subtotal / threshold) * 100));
    progressBar.style.width = `${progress}%`;
    if (subtotal >= threshold) {
        progressText.innerHTML = '🎉 You qualified for <strong>FREE Standard Shipping!</strong>';
    } else {
        const diff = (threshold - subtotal).toFixed(2);
        progressText.innerHTML = `Add <strong>$${diff}</strong> more to unlock FREE Shipping!`;
    }

    if (items.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 2.5rem 1rem;">
                <div class="empty-icon">🛒</div>
                <h4>Your Cart is Empty</h4>
                <p>Looks like you haven't added any products yet.</p>
            </div>
        `;
        subtotalEl.textContent = '$0.00';
        taxEl.textContent = '$0.00';
        totalEl.textContent = '$0.00';
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="cart-item-row" data-id="${item.productId}">
            <div class="cart-item-info">
                <div class="cart-item-title">${escapeHtml(item.productName)}</div>
                <div class="cart-item-sku">SKU: ${escapeHtml(item.productSku)}</div>
                <div class="cart-item-price">$${parseFloat(item.unitPrice).toFixed(2)} &times; ${item.quantity} = $${parseFloat(item.subtotal).toFixed(2)}</div>
            </div>
            <div class="cart-qty-control">
                <button class="qty-btn" onclick="updateCartQuantity(${item.productId}, ${item.quantity - 1})">-</button>
                <span class="qty-val">${item.quantity}</span>
                <button class="qty-btn" onclick="updateCartQuantity(${item.productId}, ${item.quantity + 1})" ${item.quantity >= item.stockAvailable ? 'disabled' : ''}>+</button>
            </div>
            <button class="cart-item-remove-btn" onclick="removeCartItem(${item.productId})" title="Remove item">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"></path>
                </svg>
            </button>
        </div>
    `).join('');

    const tax = subtotal * 0.08;
    const total = subtotal + tax;

    subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
    taxEl.textContent = `$${tax.toFixed(2)}`;
    totalEl.textContent = `$${total.toFixed(2)}`;
}

// ====================================================================
// CHECKOUT & ACID TRANSACTIONAL ORDER PLACEMENT
// ====================================================================

function openCheckoutModal() {
    const modal = document.getElementById('checkoutModal');
    const itemsContainer = document.getElementById('checkoutOrderItems');
    const subtotalEl = document.getElementById('checkoutSubtotal');
    const totalEl = document.getElementById('checkoutTotal');

    const items = state.cart.items || [];
    const subtotal = parseFloat(state.cart.totalAmount || 0);

    itemsContainer.innerHTML = items.map(i => `
        <div style="display: flex; justify-content: space-between; font-size: 0.85rem; padding: 0.25rem 0;">
            <span>${escapeHtml(i.productName)} &times; ${i.quantity}</span>
            <strong>$${parseFloat(i.subtotal).toFixed(2)}</strong>
        </div>
    `).join('');

    subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
    totalEl.textContent = `$${subtotal.toFixed(2)}`;

    modal.classList.add('open');
}

async function submitOrder() {
    const btn = document.getElementById('submitOrderBtn');
    const addr = document.getElementById('shippingAddressInput').value.trim();
    const pay = document.querySelector('input[name="paymentMethod"]:checked').value;

    if (!addr) {
        showToast('Please enter a delivery shipping address', 'warning');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Processing Transaction (ACID Lock)...';

    try {
        const res = await fetch(`${API_BASE}/orders/checkout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: state.currentUser.id,
                shippingAddress: addr,
                paymentMethod: pay
            })
        });

        if (res.ok) {
            const order = await res.json();
            document.getElementById('checkoutModal').classList.remove('open');
            showReceiptModal(order);
            loadCart(); // Refresh emptied cart
            loadProducts(); // Refresh deducted stock quantities
            showToast('Order confirmed and placed successfully!', 'success');
        } else {
            const err = await res.json();
            showToast(`Checkout Failed: ${err.message}`, 'error');
        }
    } catch (e) {
        showToast('Network error during checkout', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirm & Place Order';
    }
}

function showReceiptModal(order) {
    const modal = document.getElementById('receiptModal');
    const body = document.getElementById('receiptModalBody');

    body.innerHTML = `
        <div class="invoice-box">
            <div class="invoice-meta-row">
                <span>Order Reference:</span>
                <strong>${escapeHtml(order.orderNumber)}</strong>
            </div>
            <div class="invoice-meta-row">
                <span>Customer:</span>
                <span>${escapeHtml(state.currentUser.fullName)} (${escapeHtml(state.currentUser.email)})</span>
            </div>
            <div class="invoice-meta-row">
                <span>Shipping Address:</span>
                <span>${escapeHtml(order.shippingAddress)}</span>
            </div>
            <div class="invoice-meta-row">
                <span>Payment Method:</span>
                <span>${escapeHtml(order.paymentMethod)}</span>
            </div>
            <div class="invoice-meta-row">
                <span>Status:</span>
                <span class="status-pill status-${order.status}">${order.status}</span>
            </div>
            <div class="invoice-divider"></div>
            <div style="font-weight: 600; margin-bottom: 0.5rem;">Purchased Items:</div>
            ${order.items.map(item => `
                <div style="display:flex; justify-content: space-between; font-size: 0.85rem; padding: 0.2rem 0;">
                    <span>${escapeHtml(item.productName)} (SKU: ${item.productSku}) &times; ${item.quantity}</span>
                    <span>$${parseFloat(item.subtotal).toFixed(2)}</span>
                </div>
            `).join('')}
            <div class="invoice-divider"></div>
            <div style="display:flex; justify-content: space-between; font-size: 1.15rem; font-weight: 700;">
                <span>Grand Total:</span>
                <span class="text-success">$${parseFloat(order.totalAmount).toFixed(2)}</span>
            </div>
        </div>
    `;

    modal.classList.add('open');
}

// ====================================================================
// CUSTOMER ORDER HISTORY
// ====================================================================

function renderCustomerOrders() {
    const container = document.getElementById('customerOrdersList');
    const orders = state.customerOrders || [];

    if (orders.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📦</div>
                <h3>No Orders Yet</h3>
                <p>You haven't placed any orders with this account yet.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(o => `
        <div class="metric-card" style="flex-direction: column; align-items: stretch; margin-bottom: 1.25rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: 0.75rem;">
                <div>
                    <strong style="font-size: 1.1rem; color: #fff;">${escapeHtml(o.orderNumber)}</strong>
                    <div style="font-size: 0.78rem; color: var(--text-muted);">${o.createdAt ? o.createdAt.substring(0, 19) : ''}</div>
                </div>
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                    <span class="status-pill status-${o.status}">${o.status}</span>
                    ${o.status === 'PENDING' || o.status === 'PROCESSING' ? `
                        <button class="btn btn-secondary btn-sm" onclick="cancelCustomerOrder(${o.id})" style="color: var(--rose-500);">Cancel Order</button>
                    ` : ''}
                </div>
            </div>
            <div style="padding: 0.75rem 0;">
                ${o.items.map(i => `
                    <div style="display:flex; justify-content: space-between; font-size: 0.85rem; padding: 0.2rem 0; color: var(--text-secondary);">
                        <span>${escapeHtml(i.productName)} &times; ${i.quantity}</span>
                        <span>$${parseFloat(i.subtotal).toFixed(2)}</span>
                    </div>
                `).join('')}
            </div>
            <div style="display:flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-subtle); padding-top: 0.75rem;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Ship to: ${escapeHtml(o.shippingAddress)}</span>
                <strong style="font-size: 1.1rem; color: #fff;">Total: $${parseFloat(o.totalAmount).toFixed(2)}</strong>
            </div>
        </div>
    `).join('');
}

async function cancelCustomerOrder(orderId) {
    if (!confirm('Are you sure you want to cancel this order? Items will be restocked into inventory.')) return;
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/cancel`, { method: 'POST' });
        if (res.ok) {
            showToast('Order cancelled and inventory restored', 'info');
            loadCustomerOrders();
            loadProducts();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (e) {
        showToast('Error cancelling order', 'error');
    }
}

// ====================================================================
// ADMIN CONTROL PANEL OPERATIONS
// ====================================================================

function renderAdminStats() {
    const s = state.stats;
    document.getElementById('adminTotalRevenue').textContent = `$${parseFloat(s.totalRevenue || 0).toFixed(2)}`;
    document.getElementById('adminTotalOrders').textContent = s.totalOrders || 0;
    document.getElementById('adminTotalProducts').textContent = s.totalProducts || 0;
    document.getElementById('adminLowStockCount').textContent = s.lowStockCount || 0;
}

function renderAdminProductsTable() {
    const tbody = document.getElementById('adminProductsTableBody');
    const items = state.adminProducts || [];

    tbody.innerHTML = items.map(p => `
        <tr>
            <td><strong>#${p.id}</strong></td>
            <td>
                <strong>${escapeHtml(p.name)}</strong>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(p.description ? p.description.substring(0, 45) + '...' : '')}</div>
            </td>
            <td><code>${escapeHtml(p.sku)}</code></td>
            <td>${escapeHtml(p.categoryName || '-')}</td>
            <td>$${parseFloat(p.price).toFixed(2)}</td>
            <td>
                <span class="${p.stockQuantity <= 10 ? 'text-warning' : 'text-success'}" style="font-weight: 700;">
                    ${p.stockQuantity}
                </span>
            </td>
            <td>
                <span class="status-pill ${p.active ? 'status-DELIVERED' : 'status-CANCELLED'}">
                    ${p.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td class="text-right">
                <div class="action-btn-group">
                    <button class="btn-icon" title="Adjust Stock" onclick="promptAdjustStock(${p.id}, ${p.stockQuantity})">📦</button>
                    <button class="btn-icon" title="Edit Product" onclick="openProductModal(${p.id})">✏️</button>
                    <button class="btn-icon danger" title="Deactivate / Delete" onclick="deleteProduct(${p.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderAdminOrdersTable() {
    const tbody = document.getElementById('adminOrdersTableBody');
    const orders = state.adminOrders || [];

    tbody.innerHTML = orders.map(o => `
        <tr>
            <td><strong>${escapeHtml(o.orderNumber)}</strong></td>
            <td>${escapeHtml(o.customerName || 'Customer #' + o.customerId)}</td>
            <td>${o.items ? o.items.length : 0} item(s)</td>
            <td><strong>$${parseFloat(o.totalAmount).toFixed(2)}</strong></td>
            <td>
                <select class="filter-select" style="padding: 0.25rem 0.5rem; font-size: 0.78rem;" onchange="updateOrderStatus(${o.id}, this.value)" ${o.status === 'CANCELLED' ? 'disabled' : ''}>
                    <option value="PENDING" ${o.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="PROCESSING" ${o.status === 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                    <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                    <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                    <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                </select>
            </td>
            <td>${o.createdAt ? o.createdAt.substring(0, 10) : ''}</td>
            <td class="text-right">
                <button class="btn btn-secondary btn-sm" onclick='showReceiptModal(${JSON.stringify(o)})'>View</button>
            </td>
        </tr>
    `).join('');
}

function renderAdminCategoriesTable() {
    const tbody = document.getElementById('adminCategoriesTableBody');
    tbody.innerHTML = state.categories.map(c => `
        <tr>
            <td><strong>#${c.id}</strong></td>
            <td><strong>${escapeHtml(c.name)}</strong></td>
            <td>${escapeHtml(c.description || '-')}</td>
            <td class="text-right">
                <div class="action-btn-group">
                    <button class="btn-icon" title="Edit Category" onclick="openCategoryModal(${c.id})">✏️</button>
                    <button class="btn-icon danger" title="Delete Category" onclick="deleteCategory(${c.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Category CRUD
function openCategoryModal(categoryId = null) {
    const modal = document.getElementById('categoryModal');
    const title = document.getElementById('categoryModalTitle');
    const form = document.getElementById('categoryForm');

    if (categoryId) {
        title.textContent = 'Edit Category';
        const c = state.categories.find(item => item.id === categoryId);
        if (c) {
            document.getElementById('formCategoryId').value = c.id;
            document.getElementById('formCategoryName').value = c.name;
            document.getElementById('formCategoryDesc').value = c.description || '';
        }
    } else {
        title.textContent = 'Add New Category';
        form.reset();
        document.getElementById('formCategoryId').value = '';
    }
    modal.classList.add('open');
}

function closeCategoryModal() {
    document.getElementById('categoryModal').classList.remove('open');
}

async function saveCategory(e) {
    e.preventDefault();
    const id = document.getElementById('formCategoryId').value;
    const isEdit = !!id;

    const name = document.getElementById('formCategoryName').value.trim();
    const description = document.getElementById('formCategoryDesc').value.trim();

    if (!name) {
        showToast('Category name is required', 'warning');
        return;
    }

    const payload = { name, description };

    try {
        const url = isEdit ? `${API_BASE}/categories/${id}` : `${API_BASE}/categories`;
        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            closeCategoryModal();
            showToast(isEdit ? 'Category updated successfully!' : 'Category added successfully!', 'success');
            await loadCategories();
            populateCategorySelects();
            renderAdminCategoriesTable();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to save category', 'error');
        }
    } catch (e) {
        showToast('Network error saving category', 'error');
    }
}

async function deleteCategory(categoryId) {
    const c = state.categories.find(item => item.id === categoryId);
    const catName = c ? c.name : 'this category';
    if (!confirm(`Are you sure you want to delete the category "${catName}"?`)) return;

    try {
        const res = await fetch(`${API_BASE}/categories/${categoryId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Category deleted successfully', 'success');
            await loadCategories();
            populateCategorySelects();
            renderAdminCategoriesTable();
        } else {
            const err = await res.json();
            showToast(err.message || 'Cannot delete category (it may contain products)', 'error');
        }
    } catch (e) {
        showToast('Error deleting category', 'error');
    }
}

function renderAdminCustomersTable() {
    const tbody = document.getElementById('adminCustomersTableBody');
    const custs = state.adminCustomers || [];
    tbody.innerHTML = custs.map(u => `
        <tr>
            <td><strong>#${u.id}</strong></td>
            <td><code>${escapeHtml(u.username)}</code></td>
            <td>${escapeHtml(u.fullName)}</td>
            <td>${escapeHtml(u.email)}</td>
            <td>${escapeHtml(u.phone || '-')}</td>
            <td><span class="status-pill status-PROCESSING">${u.role}</span></td>
        </tr>
    `).join('');
}

// Product CRUD
function openProductModal(productId = null) {
    const modal = document.getElementById('productModal');
    const title = document.getElementById('productModalTitle');
    const form = document.getElementById('productForm');

    populateCategorySelects();

    if (productId) {
        title.textContent = 'Edit Product';
        const p = state.adminProducts.find(item => item.id === productId);
        if (p) {
            document.getElementById('formProductId').value = p.id;
            document.getElementById('formProductSku').value = p.sku;
            document.getElementById('formProductSku').disabled = true; // SKU immutable
            document.getElementById('formProductCategory').value = p.categoryId;
            document.getElementById('formProductName').value = p.name;
            document.getElementById('formProductDesc').value = p.description || '';
            document.getElementById('formProductPrice').value = p.price;
            document.getElementById('formProductStock').value = p.stockQuantity;
        }
    } else {
        title.textContent = 'Add New Product';
        form.reset();
        document.getElementById('formProductId').value = '';
        document.getElementById('formProductSku').disabled = false;
    }
    modal.classList.add('open');
}

async function saveProduct(e) {
    e.preventDefault();
    const id = document.getElementById('formProductId').value;
    const isEdit = !!id;

    const payload = {
        categoryId: parseInt(document.getElementById('formProductCategory').value),
        sku: document.getElementById('formProductSku').value.trim(),
        name: document.getElementById('formProductName').value.trim(),
        description: document.getElementById('formProductDesc').value.trim(),
        price: parseFloat(document.getElementById('formProductPrice').value),
        stockQuantity: parseInt(document.getElementById('formProductStock').value)
    };

    try {
        const url = isEdit ? `${API_BASE}/products/${id}` : `${API_BASE}/products`;
        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            document.getElementById('productModal').classList.remove('open');
            showToast(`Product ${isEdit ? 'updated' : 'created'} successfully!`, 'success');
            loadAdminData();
            loadProducts();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (e) {
        showToast('Error saving product', 'error');
    }
}

async function promptAdjustStock(productId, currentStock) {
    const deltaStr = prompt(`Current stock: ${currentStock}\nEnter adjustment amount (e.g. 10 to add, -5 to reduce):`);
    if (!deltaStr) return;
    const delta = parseInt(deltaStr);
    if (isNaN(delta)) {
        showToast('Invalid stock number', 'warning');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/products/${productId}/stock`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ delta: delta })
        });
        if (res.ok) {
            showToast('Stock quantity updated', 'success');
            loadAdminData();
            loadProducts();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (e) {
        showToast('Stock adjustment failed', 'error');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to deactivate/delete this product?')) return;
    try {
        const res = await fetch(`${API_BASE}/products/${productId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Product status updated', 'info');
            loadAdminData();
            loadProducts();
        }
    } catch (e) {
        showToast('Failed to delete product', 'error');
    }
}

async function updateOrderStatus(orderId, newStatus) {
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });
        if (res.ok) {
            showToast(`Order status updated to ${newStatus}`, 'success');
            loadAdminData();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (e) {
        showToast('Error updating status', 'error');
    }
}

// ====================================================================
// UTILITY HELPERS
// ====================================================================

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : type === 'warning' ? '⚠' : 'ℹ';
    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function capitalize(s) {
    if (!s) return '';
    return s.charAt(0).toUpperCase() + s.slice(1);
}
