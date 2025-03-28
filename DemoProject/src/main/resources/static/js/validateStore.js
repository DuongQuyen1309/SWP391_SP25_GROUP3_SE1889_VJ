document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".store-form");
    const name = document.getElementById("name");
    const phone = document.getElementById("phone");
    const taxCode = document.getElementById("taxCode");


    async function checkPhoneExists(phone) {
        try {
            let response = await fetch(`/api/check-phone-store?phone=${phone}`);
            if (!response.ok) throw new Error("Lỗi khi kiểm tra số điện thoại");
            let data = await response.json();
            return data.exists;
        } catch (error) {
            console.error("Lỗi khi kiểm tra số điện thoại:", error);
            return false;
        }
    }

    async function checkTaxCodeExists(taxCode) {
        try {
            let response = await fetch(`/api/check-taxcode?taxCode=${taxCode}`);
            if (!response.ok) throw new Error("Lỗi khi kiểm tra mã số thuế");
            let data = await response.json();
            return data.exists;
        } catch (error) {
            console.error("Lỗi khi kiểm tra mã số thuế:", error);
            return false;
        }
    }

    form.addEventListener("submit", async function (event) {
        // Xóa lỗi cũ trước khi kiểm tra lại
        document.querySelectorAll('.error-message').forEach(el => el.textContent = "");

        let hasError = false;
        event.preventDefault();
        if (!name.value || name.value.length <= 0) {
            document.getElementById("name-error").textContent = "Tên không được để trống.";
            document.getElementById("name-error").style.display = "block";
            hasError = true;
        } else if (!/^[A-Za-zÀ-ỹ\s]+$/.test(name.value)) {
            document.getElementById("name-error").textContent = "Tên chỉ được chứa chữ cái và khoảng trống.";
            document.getElementById("name-error").style.display = "block";
            hasError = true;
        }

        if (!phone.value || phone.value.length <= 0) {
            document.getElementById("phone-error").textContent = "Số điện thoại không được để trống.";
            document.getElementById("phone-error").style.display = "block";
            hasError = true;
        } else if (!/^0\d{9}$/.test(phone.value)) {
            document.getElementById("phone-error").textContent = "Số điện thoại phải bắt đầu bằng số 0 và có 10 số.";
            document.getElementById("phone-error").style.display = "block";
            hasError = true;
        } else {
            let isPhoneExists = await checkPhoneExists(phone.value);
            if (isPhoneExists) {
                document.getElementById("phone-error").textContent = "Số điện thoại này đã tồn tại.";
                document.getElementById("phone-error").style.display = "block";
                hasError = true;
            }
        }

        if (!taxCode.value || taxCode.value.length <= 0) {
            document.getElementById("taxCode-error").textContent = "Mã số thuế không được để trống.";
            document.getElementById("taxCode-error").style.display = "block";
            hasError = true;
        }  else if (taxCode.value.length != 13 && taxCode.value.length != 10) {
            document.getElementById("taxCode-error").textContent = "Mã số thuế phải là 10 hoặc 13 chữ số.";
            document.getElementById("taxCode-error").style.display = "block";
            hasError = true;
        }else {
            let isTaxCodeExists = await checkTaxCodeExists(taxCode.value);
            if (isTaxCodeExists) {
                document.getElementById("taxCode-error").textContent = "Mã số thuế này đã tồn tại.";
                document.getElementById("taxCode-error").style.display = "block";
                hasError = true;
            }
        }



        if (hasError) {
            event.preventDefault();
            return;
        } else {
            Swal.fire({
                icon: "success",
                title: "Thành Công!",
                text: " Thông tin cửa hàng đã được lưu thành công.",
                backdrop: false, // ❌ Tắt nền mờ phía sau
                confirmButtonText: "OK"
            }).then(() => {
                form.submit(); // Gửi form sau khi người dùng nhấn OK
            });
        }
    });
});
function enableEditing() {
    document.querySelectorAll('.store-form input, .store-form select').forEach(element => element.disabled = false);

    document.getElementById('editBtn').style.display = 'none';
    document.getElementById('cancelBtn').style.display = 'inline-block';
    document.getElementById('applyBtn').style.display = 'inline-block';

    // Lưu giá trị gốc để có thể hoàn tác nếu bấm Cancel
    sessionStorage.setItem('originalData', JSON.stringify({
        name: document.getElementById('name').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value,
        taxCode: document.getElementById('taxCode').value
    }));
}


function cancelEditing() {
    let originalData = JSON.parse(sessionStorage.getItem('originalData'));
    document.querySelectorAll('.error-message').forEach(el => el.textContent = "");


    document.getElementById('name').value = originalData.name;
    document.getElementById('phone').value = originalData.phone;
    document.getElementById('address').value = originalData.address;
    document.getElementById('taxCode').value = originalData.taxCode;

    document.querySelectorAll('.store-form input, .store-form select').forEach(element => element.disabled = true);


    document.getElementById('editBtn').style.display = 'inline-block';
    document.getElementById('cancelBtn').style.display = 'none';
    document.getElementById('applyBtn').style.display = 'none';
    document.getElementById('error-message')?.remove();
}