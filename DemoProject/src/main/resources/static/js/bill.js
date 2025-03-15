
let tabData = {}; // Lưu dữ liệu cho từng tab
let activeTabId = "tab-1"; // ID của tab đang được chọn

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

function parsePrice(priceStr) {
    return parseInt(priceStr.replace(/[,.]/g, '')) || 0;
}

function showProductModal() {
    document.getElementById("product-modal").style.display = "flex";
}

function closeProductModal() {
    document.getElementById("product-modal").style.display = "none";
}

/* Hàm tìm kiếm sản phẩm */
function searchProduct() {
    let keyword = document.getElementById("search-product").value;
    let productList = document.getElementById("product-list");
    if (keyword.length >= 1) {

        fetch(`/bill/searchImportProducts?keyword=${keyword}`)
            .then(response => response.json())
            .then(data => {
                if (data.length === 0) {
                    productList.innerHTML = "<p>Không tìm thấy sản phẩm</p>"; // Nếu không có kết quả
                } else {
                    productList.innerHTML = data.map(product => `
                        <div class="product-item" onclick="selectProduct(${product.id}, '${product.name}', ${product.price})">
                            ${product.name} - ${product.price} VND
                        </div>
                    `).join(""); // Tạo danh sách sản phẩm mới
                }
            })
            .catch(error => console.error("Lỗi tìm kiếm:", error));
    } else {
        document.getElementById("product-list").innerHTML = "<p>Nhập tên sản phẩm để tìm kiếm...</p>";
    }
}


/* Khi chọn sản phẩm, thêm vào hóa đơn và đóng modal */
function selectProduct(id, name, price) {
    if (!tabData[activeTabId])tabData[activeTabId] = { customer: {}, products: [] };

    let existingProduct = tabData[activeTabId].products.find(p => p.id === id);
    if (existingProduct) {
        alert("Sản phẩm này đã được thêm vào tab này!");
        return;
    }

    tabData[activeTabId].products.push({ id, name, price, quantity: 1 });
    document.getElementById("search-product").value = ""; // Xóa nội dung tìm kiếm
    document.getElementById("product-list").innerHTML = "<p>Nhập tên sản phẩm để tìm kiếm...</p>";
    renderProducts(activeTabId);
    updateOrderTotal(activeTabId);

    closeProductModal(); // Đóng modal sau khi chọn sản phẩm
}

// Mở popup tìm kiếm khách hàng
document.getElementById("search-customer-btn").addEventListener("click", function () {
    document.getElementById("customer-search-modal").style.display = "flex";
});

// Đóng popup tìm kiếm khách hàng
function closeCustomerSearchModal() {
    document.getElementById("customer-search-modal").style.display = "none";
}

// Mở popup thêm khách hàng
document.getElementById("add-customer-btn").addEventListener("click", function () {
    document.getElementById("add-customer-modal").style.display = "flex";
});

// Đóng popup thêm khách hàng
function closeAddCustomerModal() {
    document.getElementById("add-customer-modal").style.display = "none";
}



// Lắng nghe sự kiện thay đổi trên các ô nhập thông tin khách hàng
document.getElementById("customer-name").addEventListener("input", updateCustomerData);
document.getElementById("customer-phone").addEventListener("input", updateCustomerData);
document.getElementById("customer-address").addEventListener("input", updateCustomerData);
document.getElementById("customer-dob").addEventListener("input", updateCustomerData);
document.getElementById("customer-gender").addEventListener("input", updateCustomerData);
document.getElementById("customer-moneystate").addEventListener("input", updateCustomerData);

// Hàm cập nhật dữ liệu vào tab hiện tại
function updateCustomerData() {
    if (!tabData[activeTabId]) {
        tabData[activeTabId] = { customer: {}, products: [] };
    }

    tabData[activeTabId].customer = {
        name: document.getElementById("customer-name").value.trim(),
        phone: document.getElementById("customer-phone").value.trim(),
        address: document.getElementById("customer-address").value.trim(),
        dob: document.getElementById("customer-dob").value.trim(),
        gender: document.getElementById("customer-gender").value.trim(),
        moneystate: document.getElementById("customer-moneystate").value.trim(),


    };

    console.log(`Cập nhật khách hàng cho tab ${activeTabId}:`, tabData[activeTabId].customer);
}

function searchCustomer() {
    let keyword = document.getElementById("search-customer-input").value.trim();
    let customerList = document.getElementById("customer-list");

    if (keyword.length >= 1) {
        customerList.style.display = "block";
        customerList.innerHTML = "<p>Đang tìm kiếm...</p>";

        fetch(`/bill/searchCustomers?keyword=${keyword}`)
            .then(response => response.json())
            .then(data => {
                if (data.length === 0) {
                    customerList.innerHTML = `<p>Không tìm thấy khách hàng.</p>`;
                } else {
                    customerList.innerHTML = data.map(customer => `
                        <div class="customer-item" onclick="selectCustomer(
                            '${customer.name}',
                            '${customer.phone}',
                            '${customer.address}',
                            '${customer.dob}',
                            '${customer.gender}',
                            '${customer.moneyState}'
                        )">

                            ${customer.name} - ${customer.phone}
                        </div>

                    `).join("");
                }
            })
            .catch(error => {
                console.error("Lỗi tìm kiếm:", error);
                customerList.innerHTML = "<p>Lỗi khi tìm kiếm</p>";
            });
    } else {
        customerList.innerHTML = "";
        customerList.style.display = "none";
    }
}

// Chọn khách hàng từ danh sách
function selectCustomer(name, phone, address,dob,gender,moneystate) {
    document.getElementById("customer-name").value = name;
    document.getElementById("customer-phone").value = phone;
    document.getElementById("customer-address").value = address;
    document.getElementById("customer-dob").value = dob;
    document.getElementById("customer-gender").value = gender;
    document.getElementById("customer-moneystate").value = moneystate;
    console.log(gender)
    if (!tabData[activeTabId]) {
        tabData[activeTabId] = { customer: {}, products: [] };
    }

    // Lưu tên khách hàng ban đầu
    tabData[activeTabId].originalCustomerName = name;

    tabData[activeTabId].customer = { name, phone, address ,dob,gender ,moneystate };
    // Đặt readonly
    document.getElementById("customer-name").readOnly = true;
    document.getElementById("customer-phone").readOnly = true;
    document.getElementById("customer-address").readOnly = true;
    document.getElementById("customer-dob").readOnly = true;
    document.getElementById("customer-gender").readOnly = true;
    document.getElementById("customer-moneystate").readOnly = true;

    // Đóng popup
    closeCustomerSearchModal();
}
document.getElementById("save-customer-btn").addEventListener("click", function () {
    let name = document.getElementById("new-customer-name").value.trim();
    let phone = document.getElementById("new-customer-phone").value.trim();
    let address = document.getElementById("new-customer-address").value.trim();
    let dob = document.getElementById("new-customer-dob").value.trim();
    let gender = document.getElementById("new-customer-gender").value.trim();
    if (!name || !phone || !address) {
        alert("Vui lòng nhập đầy đủ thông tin khách hàng!");
        return;
    }

    let requestData = {
        name: name,
        phone: phone,
        address: address,
        dob: dob,
        gender: gender
    };

    fetch("/bill/addCustomer", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert("Thêm khách hàng thành công!");


                closeAddCustomerModal();
            } else {
                alert("Lỗi khi thêm khách hàng: " + data.message);
            }
        })
        .catch(error => {
            console.error("Lỗi gửi dữ liệu:", error);
            alert("Có lỗi xảy ra khi gửi dữ liệu.");
        });
});

function renderProducts(tabId) {
    let productTable = document.getElementById("selected-products");
    productTable.innerHTML = "";

    if (!tabData[tabId] || !Array.isArray(tabData[tabId].products)) {
        tabData[tabId] = { customer: {}, products: [] }; // Đảm bảo có mảng products
    }

    tabData[tabId].products.forEach((product, index) => {
        let row = `
            <tr id="product-${product.id}">
                <td>${index + 1}</td>
                <td>${product.name}</td>
                <td class="quantity-cell">
                    <span class="quantity-value">${product.quantity}</span>
                    <div class="quantity-controls">
                        <div class="quantity-btn minus" onclick="changeQuantity('${tabId}', ${product.id}, -1)">-</div>
                        <div class="quantity-btn plus" onclick="changeQuantity('${tabId}', ${product.id}, 1)">+</div>
                    </div>
                </td>
                <td class="price-cell">${formatPrice(product.price)}</td>
                <td class="price-cell total-price">${formatPrice(product.price * product.quantity)}</td>
                <td><button onclick="removeProduct('${tabId}', ${product.id})">✖</button></td>
            </tr>
        `;
        productTable.insertAdjacentHTML("beforeend", row);
    });
}

function changeQuantity(tabId, id, change) {
    let product = tabData[tabId].products.find(p => p.id === id);
    console.log(product)
    if (!product) return;

    product.quantity = Math.max(1, product.quantity + change);

    renderProducts(tabId);
    updateOrderTotal(tabId);
}

function removeProduct(tabId, id) {
    if (!tabData[tabId] || !Array.isArray(tabData[tabId].products)) return;
    tabData[tabId] = tabData[tabId].products.filter(p => p.id !== id);
    renderProducts(tabId);
    updateOrderTotal(tabId);
}

document.getElementById("loading").addEventListener("change", function () {
    let loadingAmountInput = document.getElementById("loading-amount");
    console.log(loadingAmountInput);
    if (this.checked) {
        loadingAmountInput.removeAttribute("readonly"); // Cho phép nhập tiền bốc hàng
    } else {
        loadingAmountInput.setAttribute("readonly", true); // Không cho phép nhập
        loadingAmountInput.value = "0"; // Đặt lại về 0 khi bỏ chọn
    }

    updateOrderTotal(activeTabId);
});

// Khi chọn "Trả hết", ẩn ô nhập tiền trả nợ và cập nhật số tiền khách thanh toán = tổng tiền
document.getElementById("pay-all").addEventListener("change", function () {
    if (this.checked) {
        let totalAmount = parsePrice(document.getElementById("customer-pay").textContent);
        document.getElementById("display-payment-amount").textContent = formatPrice(totalAmount);

        document.getElementById("debt-payment-container").style.display = "none"; // Ẩn ô nhập số tiền trả nợ
    }
});

// Khi chọn "Trả nợ", hiển thị ô nhập tiền trả nợ và cập nhật số tiền khách thanh toán
document.getElementById("pay-rent").addEventListener("change", function () {
    if (this.checked) {
        document.getElementById("debt-payment-container").style.display = "block"; // Hiển thị ô nhập số tiền trả nợ
        updatePaymentAmount(); // Cập nhật số tiền khách thanh toán
    }
});

// Khi nhập số tiền trả nợ, cập nhật số tiền khách thanh toán
document.getElementById("debt-payment-amount").addEventListener("input", function () {
    updatePaymentAmount();
});

// Hàm cập nhật tổng tiền khách thanh toán
function updatePaymentAmount() {
    let totalAmount = parsePrice(document.getElementById("customer-pay").textContent);
    let debtPayment = parsePrice(document.getElementById("debt-payment-amount").value);

    if (document.getElementById("pay-rent").checked) {
        document.getElementById("display-payment-amount").textContent = formatPrice(totalAmount + debtPayment);
    }
}

function updateOrderTotal(tabId) {
    let productTotal = 0; // Mặc định tổng tiền sản phẩm là 0 nếu chưa có sản phẩm nào
    if (tabData[activeTabId] && tabData[activeTabId].products) {
        productTotal = tabData[activeTabId].products.reduce((sum, product) => sum + product.price * product.quantity, 0);
    }

    // Lấy tiền bốc hàng nếu checkbox "Bốc hàng" được chọn
    let loadingFee = document.getElementById("loading").checked ? parsePrice(document.getElementById("loading-amount").value) : 0;

    let finalTotal = productTotal + loadingFee;

    document.getElementById("total-amount").textContent = formatPrice(finalTotal);
    document.getElementById("customer-pay").textContent = formatPrice(finalTotal);
    // Nếu đang chọn "Trả hết", cập nhật lại số tiền khách thanh toán
    if (document.getElementById("pay-all").checked) {
        document.getElementById("display-payment-amount").textContent = formatPrice(finalTotal);
    } else {
        updatePaymentAmount(); // Nếu chọn "Trả nợ", cập nhật theo số tiền nhập
    }
}
// Gán sự kiện khi nhập tiền bốc hàng sẽ cập nhật tổng tiền ngay lập tức
document.getElementById("loading-amount").addEventListener("input", function () {
    if (document.getElementById("loading").checked) {
        updateOrderTotal(); // Cập nhật tổng tiền ngay khi nhập giá trị
    }
});

// Xử lý tạo và chuyển tab
document.querySelector(".add-tab").addEventListener("click", function () {
    let existingNumbers = [...document.querySelectorAll(".tab")]
        .map(tab => parseInt(tab.querySelector(".tab-title").textContent.replace("Hóa đơn ", "")))
        .filter(n => !isNaN(n)); // Lấy danh sách các số hóa đơn hiện tại

    let newNumber = 1;
    while (existingNumbers.includes(newNumber)) {
        newNumber++; // Tìm số nhỏ nhất chưa được dùng
    }
    let tabId = `tab-${Date.now()}`; // Sử dụng timestamp để tạo ID duy nhất
    tabData[tabId] = { customer: {}, products: [] }; // Đúng cú pháp

    let newTab = document.createElement("div");
    newTab.className = "tab";
    newTab.setAttribute("data-tab", tabId);
    newTab.innerHTML = `
        <span class="tab-title">Hóa đơn ${newNumber}</span>
        <span class="tab-close">×</span>
    `;

    newTab.addEventListener("click", function () {
        switchTab(tabId);
    });

    newTab.querySelector(".tab-close").addEventListener("click", function (e) {
        e.stopPropagation();
        delete tabData[tabId];
        newTab.remove();
        if (document.querySelectorAll(".tab").length > 0) {
            let firstTab = document.querySelector(".tab");
            switchTab(firstTab.getAttribute("data-tab"));
        }
    });

    // ✅ Chèn tab vào đúng vị trí thay vì chỉ đặt trước dấu +
    let tabContainer = document.querySelector(".tab-container");
    let tabs = [...tabContainer.querySelectorAll(".tab")];

    let inserted = false;
    for (let tab of tabs) {
        let tabNumber = parseInt(tab.querySelector(".tab-title").textContent.replace("Hóa đơn ", ""));
        if (newNumber < tabNumber) {
            tabContainer.insertBefore(newTab, tab);
            inserted = true;
            break;
        }
    }

    if (!inserted) {
        tabContainer.insertBefore(newTab, document.querySelector(".add-tab"));
    }

    switchTab(tabId);
});

function switchTab(tabId) {
    activeTabId = tabId;
    document.querySelectorAll(".tab").forEach(tab => tab.classList.remove("active"));
    document.querySelector(`[data-tab="${tabId}"]`).classList.add("active");

    renderProducts(tabId);
    updateOrderTotal(tabId);
    // Reset thông tin khách hàng khi chuyển tab
    loadCustomerData(tabId);
}
function loadCustomerData(tabId) {
    let customer = tabData[tabId]?.customer || {};
    console.log(customer.moneystate)
    document.getElementById("customer-name").value = customer.name || "";
    document.getElementById("customer-phone").value = customer.phone || "";
    document.getElementById("customer-address").value = customer.address || "";
    document.getElementById("customer-dob").value = customer.dob || "";
    document.getElementById("customer-gender").value = customer.gender || "";
    document.getElementById("customer-moneystate").value = customer.moneystate || "";
}

document.querySelector(".checkout-button").addEventListener("click", function () {
    if (!tabData[activeTabId]) {
        alert("Không có dữ liệu để thanh toán!");
        return;
    }

    let customerData = tabData[activeTabId]?.customer || {};
    let productData = tabData[activeTabId]?.products || [];

    if (!customerData.name || productData.length === 0) {
        alert("Vui lòng nhập thông tin khách hàng và thêm sản phẩm!");
        return;
    }

    let requestData = {
        customer: customerData,
        products: productData
    };

    fetch("/bill/submitBill", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert("Thanh toán thành công!");
                removeTab(activeTabId); // Xóa tab đã xử lý
            } else {
                alert("Lỗi khi xử lý thanh toán: " + data.message);
            }
        })
        .catch(error => {
            console.error("Lỗi gửi dữ liệu:", error);
            alert("Có lỗi xảy ra khi gửi dữ liệu.");
        });
});