document.addEventListener("DOMContentLoaded", function () {
    let form = document.getElementById("loginForm");
    let usernameInput = document.getElementById("username");
    let passwordInput = document.getElementById("password");

    const generalError = document.getElementById("general-error");

    form.addEventListener("submit", async function (event) {
        event.preventDefault(); // Ngăn form submit mặc định
        document.querySelectorAll('.error-message').forEach(el => el.textContent = "");
        generalError.textContent = "";

        let hasError = false;

        // ✅ Kiểm tra username không rỗng
        if (usernameInput.value.trim() === "") {
            document.getElementById("username-error").textContent = "Tài khoản không được để trống.";
            hasError = true;

        }

        // ✅ Kiểm tra password không rỗng
        if (passwordInput.value.trim() === "") {
            document.getElementById("password-error").textContent = "Mật khẩu không được để trống.";
            hasError = true;
        }

        // ✅ Nếu có lỗi, hiển thị lỗi và ngăn form submit
        if (hasError) {
            event.preventDefault();
            return;
        }

        // ✅ Gửi request fetch kiểm tra username & password
        try {
            let response = await fetch("http://localhost:8081/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    username: usernameInput.value,
                    password: passwordInput.value
                })
            });

            if (!response.ok) {
                let errorData = await response.json();
                throw new Error(errorData.error || "Sai tài khoản hoặc mật khẩu!");
            }

            let data = await response.json();
            console.log("✅ Đăng nhập thành công!", data);


            window.location.href = "/home"; // hoặc đường dẫn chính của hệ thống


        } catch (error) {
            console.error("❌ Login failed:", error);
            generalError.textContent = error.message || "Có lỗi xảy ra.";
        }
    });
});