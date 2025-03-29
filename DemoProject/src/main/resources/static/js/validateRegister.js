document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".register-form");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const displaynameInput = document.getElementById("displayname");
    const emailInput = document.getElementById("email");

    // ✅ Hàm gửi request AJAX kiểm tra username có tồn tại không
    async function checkUsernameExists(username) {
        try {
            let response = await fetch(`/api/check-username?username=${username}`);
            let data = await response.json();
            return data.exists;
        } catch (error) {
            console.error("Lỗi check username:", error);
            return false;
        }
    }
    async function checkEmailExists(email) {
        try {
            let response = await fetch(`/api/check-email?email=${email}`);
            let data = await response.json();
            return data.exists;
        } catch (error) {
            console.error("Error when checking email:", error);
            return false;
        }
    }
    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        // Xóa lỗi cũ trước khi kiểm tra lại
        document.querySelectorAll('.error-message').forEach(el => el.textContent = "");

        let hasError = false;


        if(username.value.length > 50 || username.value.length < 3) {
            document.getElementById("username-error").textContent = "Tài khoản phải lớn hơn 3 và không được vượt quá 50 kí tự.";
            hasError = true;
        } else if (!/^[A-Za-z0-9]+$/.test(username.value)) {
            document.getElementById("username-error").textContent = "Tài khoản chỉ chứa chữ hoặc số và không có kí tự đặc biệt.";
            hasError=true;

        } else {
            // 🔍 Gửi AJAX kiểm tra username có tồn tại không
            let isUsernameExists = await checkUsernameExists(usernameInput.value);
            console.log(isUsernameExists);
            if (isUsernameExists) {
                document.getElementById("username-error").textContent ="Tài khoản đã tồn tại.";
                hasError=true;
            }
        }

        // ✅ Kiểm tra password không rỗng và có đủ điều kiện
        if (passwordInput.value.length <3 || passwordInput.value.length > 20 ) {
            document.getElementById("password-error").textContent ="Mật khẩu phải lớn hơn 3 và không được vượt quá 20 kí tự.";
            hasError=true;
        }

        // ✅ Kiểm tra display name không rỗng
        if (displaynameInput.value.trim() === "") {
            document.getElementById("displayname-error").textContent ="Tên hiển thị không được để trống.";
            hasError=true;
        } else if(displaynameInput.value.length > 50){
            document.getElementById("username-error").textContent = "Tên hiển thị được vượt quá 50 kí tự.";
            hasError=true;
        }

        if (emailInput.value.trim() === "" || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput.value)) {
            document.getElementById("email-error").textContent ="Sai cú pháp email.";
            hasError=true;
        } else if(emailInput.value.length > 50){
            document.getElementById("username-error").textContent = "Email được vượt quá 50 kí tự.";
            hasError=true;
        } else{
            let isEmailExists = await checkEmailExists(emailInput.value);
            console.log(isEmailExists);
            if (isEmailExists) {
                document.getElementById("email-error").textContent ="Email đã tồn tại.";
                hasError=true;
            }
        }

        if (hasError) {
            event.preventDefault();
            return;
        } else {
            Swal.fire({
                icon: "success",
                title: "Thành Công!",
                text: "Bạn đã tạo tài khoản thành công.",
                backdrop: false, // ❌ Tắt nền mờ phía sau
                confirmButtonText: "OK"
            }).then(() => {
                form.submit(); // Gửi form sau khi người dùng nhấn OK
            });
        }

    });


});