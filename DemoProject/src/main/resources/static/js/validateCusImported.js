function validateName(input) {
    const nameRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s]+$/;
    const errorElement = document.getElementById("name-error");
    const value = input.value.trim();

    if (value.length < 3 || value.length > 50) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên khách hàng phải từ 3 đến 50 ký tự!";
        return false;
    } else if (!nameRegex.test(value)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên khách hàng chứa ký tự không hợp lệ!";
        return false;
    }

    errorElement.style.display = "none";
    return true;
}

function validatePhone(input) {
    const errorElement = document.getElementById("phone-error");
    const phoneRegex = /^(0\d{9,10})$/;
    const value = input.value.trim();

    if (!phoneRegex.test(value)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Số điện thoại không hợp lệ! Nhập 10-11 số bắt đầu bằng 0.";
        return false;
    }

    errorElement.style.display = "none";
    return true;
}

function validateAddress(input) {
    const errorElement = document.getElementById("address-error");
    const addressRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s,.-]{5,100}$/;
    const value = input.value.trim();

    if (!addressRegex.test(value)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Địa chỉ không hợp lệ! Nhập từ 5 đến 100 ký tự.";
        return false;
    }

    errorElement.style.display = "none";
    return true;
}

function validateDob(input) {
    const errorElement = document.getElementById("dob-error");
    const dobValue = input.value.trim();

    const dob = new Date(dobValue);
    const today = new Date();

    if (dob > today) {
        errorElement.style.display = "block";
        errorElement.textContent = "Ngày sinh không thể ở tương lai!";
        return false;
    }

    errorElement.style.display = "none";
    return true;
}

async function validateAndSubmitForm(event) {
    event.preventDefault();

    const nameInput = document.getElementById("new-customer-name");
    const phoneInput = document.getElementById("new-customer-phone");
    const addressInput = document.getElementById("new-customer-address");
    const dobInput = document.getElementById("new-customer-dob");
    const genderInput = document.getElementById("new-customer-gender");
    const form = document.getElementById("addCustomer");

    const isNameValid = validateName(nameInput);
    const isPhoneValid = validatePhone(phoneInput);
    const isAddressValid = validateAddress(addressInput);
    const isDobValid = validateDob(dobInput);

    if (!(isNameValid && isPhoneValid && isAddressValid && isDobValid)) {
        return; // Dừng lại nếu có lỗi
    }

    // Chuẩn bị dữ liệu gửi đi
    const requestData = {
        name: nameInput.value.trim(),
        phone: phoneInput.value.trim(),
        address: addressInput.value.trim(),
        dob: dobInput.value.trim(),
        gender: genderInput.value.trim()
    };

    try {
        const response = await fetch("/bill/addCustomer", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestData)
        });

        const data = await response.json();

        if (data.success) {
            Swal.fire({
                title: "Thành công!",
                text: "Khách hàng đã được thêm thành công.",
                icon: "success",
                confirmButtonText: "OK"
            }).then(() => {
                form.reset();
                closeAddCustomerModal();
            });
        } else {
            Swal.fire({
                title: "Lỗi!",
                text: data.message,
                icon: "error",
                confirmButtonText: "OK"
            });
        }
    } catch (error) {
        console.error("Lỗi gửi dữ liệu:", error);
        Swal.fire({
            title: "Lỗi!",
            text: "Có lỗi xảy ra khi gửi dữ liệu.",
            icon: "error",
            confirmButtonText: "OK"
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const customerForm = document.getElementById("addCustomer");
    customerForm.addEventListener("submit", validateAndSubmitForm);
});
