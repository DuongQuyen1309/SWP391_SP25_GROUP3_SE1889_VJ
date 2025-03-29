// Khai báo các biến toàn cục
let zones = [];
let customer = {};
let products = [];
let payment = {totalDebt: 0, customerPayment: 0, loading: false, loadingAmount: 0};

// Lấy danh sách zone từ server
fetch('/warehouse/getZones')
    .then(response => response.json())
    .then(data => {
        zones = data; // Lưu danh sách zone với id và name
        renderProducts(); // Render lại nếu cần
    })
    .catch(error => console.error("Lỗi lấy danh sách zone:", error));

// Hàm định dạng giá
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

// Hàm phân tích giá từ chuỗi
function parsePrice(priceStr) {
    if (!priceStr) return 0;
    if (priceStr === "0") return 0;
    return parseInt(priceStr.replace(/,/g, '').replace(/[^\d-]/g, '')) || 0;
}

// Hiển thị và đóng modal sản phẩm
function showProductModal() {
    document.getElementById("product-modal").style.display = "flex";
}

function closeProductModal() {
    document.getElementById("product-modal").style.display = "none";
}

// Ngăn chỉnh sửa các trường khách hàng
document.getElementById("customer-name").readOnly = true;
document.getElementById("customer-address").readOnly = true;
document.getElementById("customer-phone").readOnly = true;
document.getElementById("customer-gender").readOnly = true;
document.getElementById("customer-dob").readOnly = true;
document.getElementById("customer-moneystate").readOnly = true;
document.getElementById("customer-totaldebt").readOnly = true;

['customer-name', 'customer-address', 'customer-phone', 'customer-gender', 'customer-dob', 'customer-moneystate', 'customer-totaldebt'].forEach(id => {
    document.getElementById(id).addEventListener("keydown", function (event) {
        event.preventDefault();
    });
    document.getElementById(id).addEventListener("contextmenu", function (event) {
        event.preventDefault();
    });
});

function searchProduct() {
    let keyword = document.getElementById("search-product").value;
    let productList = document.getElementById("product-list");
    if (keyword.length >= 1) {

        fetch(`/product/searchProducts?keyword=${keyword}`)
            .then(response => response.json())
            .then(data => {
                if (data.length === 0) {
                    productList.innerHTML = "<p>Không tìm thấy sản phẩm</p>"; // Nếu không có kết quả
                } else {
                    productList.innerHTML = data.map(product => `
                    <div class="product-item" 
                        onclick="selectProduct(${product.id}, '${product.name}', ${product.price}, '${encodeURIComponent(product.image || '')}')">
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


// Chọn sản phẩm
function selectProduct(id, name, price, image) {
    image = decodeURIComponent(image || ''); // Giải mã giá trị image
    let existingProduct = products.find(p => p.id === id);
    if (existingProduct) {
        alert("Sản phẩm này đã được thêm!");
        return;
    }
    products.push({id, name, price, quantity: 1, zoneId: null, image});
    document.getElementById("search-product").value = "";
    document.getElementById("product-list").innerHTML = "<p>Nhập tên sản phẩm để tìm kiếm...</p>";
    renderProducts();
    updateOrderTotal();
    updateTotalDebt();
    closeProductModal();
}

// Render danh sách sản phẩm
function renderProducts() {
    let productTable = document.getElementById("selected-products");
    productTable.innerHTML = "";
    products.forEach((product, index) => {
        let row = `
            <tr id="product-${product.id}">
                <td>${index + 1}</td>
                <td>${product.name}
                    <input type="hidden" name="productImage-${product.id}" value="${product.image}">
                </td>
                <td>${product.price}</td>
                <td class="quantity-cell">
                    <span class="quantity-value">${product.quantity}</span>
                    <div class="quantity-controls">
                        <div class="quantity-btn minus" onclick="changeQuantity(${product.id}, -1)">-</div>
                        <div class="quantity-btn plus" onclick="changeQuantity(${product.id}, 1)">+</div>
                    </div>
                </td>
                <td class="price-cell">
                    <input style="width: 60px" type="text" class="unit-price-input" value="${formatPrice(product.price)}"
                    onchange="updateUnitPrice(${product.id}, this.value)"
                    oninput="validateNumber(this)"
                    >
                </td>
                <td style="text-align: center" class="price-cell total-price">${formatPrice(product.price * product.quantity)}</td>
                <td>
                    <select class="zone-select" onchange="updateProductZone(${product.id}, this.value)">
                        <option value="">Chọn zone</option>
                        ${zones.map(zone => `<option value="${zone.id}" ${product.zoneId === zone.id ? 'selected' : ''}>${zone.name}</option>`).join('')}
                    </select>
                </td>
                <td><button onclick="removeProduct(${product.id})">✖</button></td> 
            </tr>
        `;
        productTable.insertAdjacentHTML("beforeend", row);
    });
}


// Tìm kiếm khách hàng
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
                    customerList.innerHTML = `<p>Không có khách hàng.</p>`;
                } else {
                    customerList.innerHTML = data.map(customer => `
                        <div class="customer-item" onclick="selectCustomer(
                            '${customer.name}',
                            '${customer.phone}',
                            '${customer.address}',
                            '${customer.dob}',
                            '${customer.gender}'
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

// Chọn khách hàng
function selectCustomer(name, phone, address, dob, gender, moneystate) {
    customer = {name, phone, address, dob, gender, moneystate};

    document.getElementById("customer-name").value = name;
    document.getElementById("customer-phone").value = phone;
    document.getElementById("customer-address").value = address;
    document.getElementById("customer-dob").value = dob;
    document.getElementById("customer-gender").value = gender;
    document.getElementById("customer-moneystate").value = moneystate;
    document.getElementById("customer-totaldebt").value = moneystate;
    closeCustomerSearchModal();
    updateTotalDebt();
}


function formatDate(dateStr) {
    let parts = dateStr.split("-"); // Tách chuỗi theo dấu "-"
    return `${parts[2]}-${parts[1]}-${parts[0]}`; // Đảo vị trí thành dd-mm-yyyy
}


// Cập nhật đơn giá
function updateUnitPrice(productId, newPriceStr) {
    let product = products.find(p => p.id === productId);
    if (product) {
        let newPrice = parsePrice(newPriceStr);
        if (isNaN(newPrice) || newPrice < 0) {
            alert("Giá không hợp lệ!");
            return;
        }
        product.price = newPrice;
        renderProducts();
        updateOrderTotal();
        updateTotalDebt();
    }
}

// Cập nhật zone cho sản phẩm
function updateProductZone(productId, zoneId) {
    let product = products.find(p => p.id === productId);
    if (product) {
        product.zoneId = zoneId ? parseInt(zoneId) : null;
    }
}

// Thay đổi số lượng sản phẩm
function changeQuantity(id, change) {
    let product = products.find(p => p.id === id);
    if (product) {
        product.quantity = Math.max(1, product.quantity + change);
        renderProducts();
        updateOrderTotal();
        updateTotalDebt();
    }
}

// Xóa sản phẩm
function removeProduct(id) {
    products = products.filter(p => p.id !== id);
    renderProducts();
    updateOrderTotal();
    updateTotalDebt();
}

// Sự kiện checkbox "Bốc hàng"
document.getElementById("loading").addEventListener("change", function () {
    let loadingAmountInput = document.getElementById("loading-amount");
    if (this.checked) {
        loadingAmountInput.removeAttribute("readonly");
    } else {
        loadingAmountInput.setAttribute("readonly", true);
        loadingAmountInput.value = "0";
    }
    updateOrderTotal();
    updateTotalDebt();
});

// Cập nhật tổng công nợ khi nhập số tiền thanh toán
document.getElementById("customer-payment").addEventListener("input", function (event) {
    let inputValue = event.target.value.trim();
    let errorSpan = document.getElementById("customer-payment-error");
    let oldDebt = parsePrice(customer.moneystate || "0");
    if (inputValue === "") {
        errorSpan.textContent = "";
        event.target.classList.remove("input-error");
        document.getElementById("customer-totaldebt").value = formatPrice(oldDebt);
        return;
    }
    if (!/^\d+$/.test(inputValue)) {
        errorSpan.textContent = "Vui lòng nhập số nguyên dương!";
        event.target.classList.add("input-error");
        return;
    } else {
        errorSpan.textContent = "";
        event.target.classList.remove("input-error");
    }
    updateTotalDebt();
});

// Cập nhật tổng công nợ
function updateTotalDebt() {
    let customerName = document.getElementById("customer-name").value.trim();
    let oldDebt = parsePrice(customer.moneystate || "0");
    let payment = parsePrice(document.getElementById("customer-payment").value);
    let totalAmount = parsePrice(document.getElementById("total-amount").textContent) || 0;

    if (customerName === "") {
        document.getElementById("customer-totaldebt").value = "";
        return;
    }

    if (payment >= 0) {
        let totalDebt = oldDebt + (payment - totalAmount);
        document.getElementById("customer-totaldebt").value = formatPrice(totalDebt);
    } else {
        document.getElementById("customer-totaldebt").value = formatPrice(oldDebt);
    }
}

// Cập nhật tổng tiền
function updateOrderTotal() {
    let productTotal = products.reduce((sum, product) => sum + product.price * product.quantity, 0);
    let loadingFee = document.getElementById("loading").checked ? parsePrice(document.getElementById("loading-amount").value) : 0;
    let finalTotal = productTotal + loadingFee;
    document.getElementById("total-amount").textContent = formatPrice(finalTotal);
    document.getElementById("customer-pay").textContent = formatPrice(finalTotal);
}

// Sự kiện nhập tiền bốc hàng
document.getElementById("loading-amount").addEventListener("input", function (event) {
    if (document.getElementById("loading").checked) {
        let inputValue = event.target.value;
        let errorSpan = document.getElementById("loading-amount-error");
        if (inputValue === "") {
            errorSpan.textContent = "";
            event.target.classList.remove("input-error");
            updateOrderTotal();
            updateTotalDebt();
            return;
        }
        if (!/^\d+$/.test(inputValue)) {
            errorSpan.textContent = "Vui lòng nhập số nguyên dương!";
            event.target.classList.add("input-error");
            return;
        } else {
            errorSpan.textContent = "";
            event.target.classList.remove("input-error");
        }
        updateOrderTotal();
        updateTotalDebt();
    }
});


// Ngăn chỉnh sửa các trường khách hàng
document.getElementById("customer-name").readOnly = true;
document.getElementById("customer-address").readOnly = true;
document.getElementById("customer-phone").readOnly = true;
document.getElementById("customer-gender").readOnly = true;
document.getElementById("customer-dob").readOnly = true;
document.getElementById("customer-moneystate").readOnly = true;

['customer-name', 'customer-address', 'customer-phone', 'customer-gender', 'customer-dob'].forEach(id => {
    document.getElementById(id).addEventListener("keydown", function (event) {
        event.preventDefault();
    });
    document.getElementById(id).addEventListener("contextmenu", function (event) {
        event.preventDefault();
    });
});

// Sự kiện nút tìm kiếm và thêm khách hàng
document.getElementById("search-customer-btn").addEventListener("click", function () {
    document.getElementById("customer-search-modal").style.display = "flex";
});

function closeCustomerSearchModal() {
    document.getElementById("customer-search-modal").style.display = "none";
}

document.getElementById("add-customer-btn").addEventListener("click", function () {
    document.getElementById("add-customer-modal").style.display = "flex";
});

function closeAddCustomerModal() {
    document.getElementById("add-customer-modal").style.display = "none";
}

function validateNumber(input) {
    let value = input.value;

    let quantity = parseInt(value, 10);

    if (isNaN(quantity) || quantity < 1) {
        alert("Đơn giá không hợp lệ!!!")
    }
}


// Sự kiện checkbox "Bốc hàng"
document.getElementById("loading").addEventListener("change", function () {
    let loadingAmountInput = document.getElementById("loading-amount");
    if (this.checked) {
        loadingAmountInput.removeAttribute("readonly");
    } else {
        loadingAmountInput.setAttribute("readonly", true);
        loadingAmountInput.value = "0";
    }
    updateOrderTotal();
    updateTotalDebt();
});

// Cập nhật tổng công nợ khi nhập số tiền thanh toán
document.getElementById("customer-payment").addEventListener("input", function (event) {
    let inputValue = event.target.value.trim();
    let errorSpan = document.getElementById("customer-payment-error");
    let moneyState = parsePrice(customer.moneyState || "0");
    if (inputValue === "") {
        errorSpan.textContent = "";
        event.target.classList.remove("input-error");
        document.getElementById("customer-totaldebt").value = formatPrice(oldDebt);
        return;
    }
    if (!/^\d+$/.test(inputValue)) {
        errorSpan.textContent = "Vui lòng nhập số nguyên dương!";
        event.target.classList.add("input-error");
        return;
    } else {
        errorSpan.textContent = "";
        event.target.classList.remove("input-error");
    }
    updateTotalDebt();
});

// Cập nhật tổng công nợ
function updateTotalDebt() {
    let customerName = document.getElementById("customer-name").value.trim();
    let payment = parsePrice(document.getElementById("customer-payment").value);
    let moneyFinal = document.getElementById("customer-moneystate").value.trim();
    let totalAmount = parsePrice(document.getElementById("total-amount").textContent) || 0;

    if (customerName === "") {
        document.getElementById("customer-totaldebt").value = "";
        return;
    }

    if (payment >= 0) {
        moneyFinal = totalAmount-payment;
        document.getElementById("customer-moneystate").value = formatPrice(moneyFinal);
    }else{
        document.getElementById("customer-moneystate").value = 0;
    }
}

// Cập nhật tổng tiền
function updateOrderTotal() {
    let productTotal = products.reduce((sum, product) => sum + product.price * product.quantity, 0);
    let loadingFee = document.getElementById("loading").checked ? parsePrice(document.getElementById("loading-amount").value) : 0;
    let finalTotal = productTotal + loadingFee;
    document.getElementById("total-amount").textContent = formatPrice(finalTotal);
    document.getElementById("customer-pay").textContent = formatPrice(finalTotal);
}

// Sự kiện nhập tiền bốc hàng
document.getElementById("loading-amount").addEventListener("input", function (event) {
    if (document.getElementById("loading").checked) {
        let inputValue = event.target.value;
        let errorSpan = document.getElementById("loading-amount-error");
        if (inputValue === "") {
            errorSpan.textContent = "";
            event.target.classList.remove("input-error");
            updateOrderTotal();
            updateTotalDebt();
            return;
        }
        if (!/^\d+$/.test(inputValue)) {
            errorSpan.textContent = "Vui lòng nhập số nguyên dương!";
            event.target.classList.add("input-error");
            return;
        } else {
            errorSpan.textContent = "";
            event.target.classList.remove("input-error");
        }
        updateOrderTotal();
        updateTotalDebt();
    }
});

// Xử lý thanh toán
document.querySelector(".checkout-button").addEventListener("click", function () {
    if (!customer.name || products.length === 0) {
        alert("Vui lòng nhập thông tin khách hàng và thêm sản phẩm!");
        return;
    }

    for (let product of products) {
        if (isNaN(product.price) || product.price <= 0) {
            alert("Giá sản phẩm không hợp lệ!");
            return;
        }
        if (!product.zoneId) {
            alert("Vui lòng chọn zone cho tất cả sản phẩm!");
            return;
        }
    }

    let productList = products.map(product => ({
        id: product.id,
        image : product.image,
        name : product.name,
        quantity: product.quantity,
        price: product.price,
        subtotal: product.price * product.quantity,
        zoneId: product.zoneId,
    }));

    let requestData = {
        paidMoney : document.getElementById("customer-payment").value || 0,
        debtMoney : parsePrice(document.getElementById("customer-moneystate").value) || 0,
        portedMoney : document.getElementById("loading-amount").value || "0",
        importDate: document.getElementById("importDate").value,
        customerPhone : document.getElementById("customer-phone").value,
        productData: productList
    };

    fetch("/product/import", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
                return;
            }
            if (!response.ok) {
                throw new Error(`Lỗi từ server: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {

            if (data && data.success) { // Kiểm tra data tồn tại
                alert("Thanh toán thành công!");
                // Reset form
                customer = {};
                products = [];
                document.getElementById("customer-name").value = "";
                document.getElementById("customer-phone").value = "";
                document.getElementById("customer-address").value = "";
                document.getElementById("customer-dob").value = "";
                document.getElementById("customer-gender").value = "";
                document.getElementById("customer-moneystate").value = "";
                document.getElementById("customer-totaldebt").value = "";
                document.getElementById("customer-payment").value = "";
                document.getElementById("loading").checked = false;
                document.getElementById("loading-amount").value = "0";
                document.getElementById("loading-amount").setAttribute("readonly", true);
                renderProducts();
                updateOrderTotal();
            } else if (data) {
                alert("Lỗi khi xử lý thanh toán: " + (data.message || "Không có thông tin lỗi"));
            }
        })
        .catch(error => {
            console.error("Lỗi chi tiết:", error);
            alert("Có lỗi xảy ra khi gửi dữ liệu: " + error.message);
        });

});

