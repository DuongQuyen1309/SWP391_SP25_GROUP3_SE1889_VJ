document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".changepw-form");
    const usernameInput = document.getElementById("username");
    let username = document.getElementById("username");

    // Đảm bảo input luôn bị vô hiệu hóa
    username.disabled = true;

    const oldPasswordInput = document.getElementById("oldPassword");
    const newPasswordInput = document.getElementById("newPassword");
    const confirmPasswordInput = document.getElementById("confirmPassword");



    // ✅ Hàm gửi request AJAX kiểm tra old password có đúng không
    async function checkOldPassword(username, oldPassword) {
        try {
            let response = await fetch(`/api/check-old-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username: username, oldPassword: oldPassword })
            });

            let data = await response.json();
            return data.valid; // Trả về true nếu mật khẩu đúng
        } catch (error) {
            console.error("Lỗi khi kiểm tra mật khẩu cũ:", error);
            return false;
        }
    }

    form.addEventListener("submit", async function (event) {
        document.querySelectorAll('.error-message').forEach(el => el.textContent = "");

        let hasError = false;
        event.preventDefault(); // Ngăn chặn submit ngay lập tức




        // ✅ Kiểm tra old password
        if (oldPasswordInput.value.trim() === "") {
            document.getElementById("oldPassword-error").textContent = "Mật khẩu cũ không được để trống.";
            document.getElementById("oldPassword-error").style.display = "block";
            hasError=true;
        } else {
            let isOldPasswordCorrect = await checkOldPassword(usernameInput.value, oldPasswordInput.value);
            console.log(isOldPasswordCorrect);
            if (!isOldPasswordCorrect) {
                document.getElementById("oldPassword-error").textContent = "Mật khẩu cũ không chính xác.";
                document.getElementById("oldPassword-error").style.display = "block";
                hasError=true;
            }
        }

        // ✅ Kiểm tra new password
        if (newPasswordInput.value.length < 3 || newPasswordInput.value.length>50) {
            document.getElementById("newPassword-error").textContent = "Mật khẩu mới phải từ 3 đến 50 kí tự.";
            document.getElementById("newPassword-error").style.display = "block";
            hasError=true;
        }

        // ✅ Kiểm tra confirm password
        if (confirmPasswordInput.value.length < 3 || confirmPasswordInput.value.length>50) {
            document.getElementById("confirmPassword-error").textContent = "Mật khẩu xác nhận phải từ 3 đến 50 kí tự.";
            document.getElementById("confirmPassword-error").style.display = "block";
            hasError=true;
        } else if (newPasswordInput.value !== confirmPasswordInput.value) {
            document.getElementById("confirmPassword-error").textContent = "Mật khẩu mới và xác nhận mật khẩu phải giống nhau.";
            document.getElementById("confirmPassword-error").style.display = "block";
            hasError=true;
        }

        if (hasError) {
            event.preventDefault();
            return;
        } Swal.fire({
            icon: "success",
            title: "Thành Công!",
            text: "Bạn đã đổi mật khẩu thành công.",
            backdrop: false, // ❌ Tắt nền mờ phía sau
            confirmButtonText: "OK"
        }).then(() => {
            form.submit(); // Gửi form sau khi người dùng nhấn OK
        });
    });
});