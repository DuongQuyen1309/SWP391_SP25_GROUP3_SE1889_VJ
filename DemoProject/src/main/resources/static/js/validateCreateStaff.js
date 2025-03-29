
function validateName(input) {
    const nameRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s]+$/;
    const errorElement = document.getElementById("usernameError");
    const minlength = 3;
    const maxlength = 50;
    if (input.value.trim().length < 3 || input.value.trim().length > 50) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên ít nhất 3 kí tự và ít hơn 50 kí tự!"
        return false;
    } else {
        if (!nameRegex.test(input.value)) {
            errorElement.style.display = "block";
            errorElement.textContent = "Tên có chứa kí tự không hợp lệ!"
        }
        errorElement.style.display = "none";
        return true;
    }
}

function validatePass(input) {
    const errorElement = document.getElementById("passError");
    const minLength = 3;
    const maxLength = 20;

    if (input.value.length < minLength || input.value.length > maxLength) {
        errorElement.style.display = "block";
        errorElement.textContent = "Mật khẩu phải từ 3 đến 20 kí tự!";
        return false;
    }  else {
        errorElement.style.display = "none";
        return true;
    }
}

function validateDisplayName(input) {
    const errorElement = document.getElementById("displaynameError");
    const nameRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s]+$/;
    const minLength = 3;
    const maxLength = 50;

    if (input.value.trim().length < minLength || input.value.trim().length > maxLength) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên hiển thị phải từ 3 đến 50 kí tự!";
        return false;
    } else if (!nameRegex.test(input.value)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên hiển thị có chứa kí tự không hợp lệ!";
        return false;
    } else {
        errorElement.style.display = "none";
        return true;
    }
}

function validateEmail(input) {
    const errorElement = document.getElementById("emailError");
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!emailRegex.test(input.value)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Email không hợp lệ!";
        return false;
    } else {
        errorElement.style.display = "none";
        return true;
    }
}
async function checkUsernameExists(username) {
    try {
        let response = await fetch(`/api/check-username?username=${username}`);
        let data = await response.json();
        return data.exists;
    } catch (error) {
        console.error("Lỗi khi kiểm tra username:", error);
        return false;
    }
}

async function checkEmailExists(email) {
    try {
        let response = await fetch(`/api/check-email?email=${email}`);
        let data = await response.json();
        return data.exists;
    } catch (error) {
        console.error("Lỗi khi kiểm tra email:", error);
        return false;
    }
}

async function validateForm() {
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const displaynameInput = document.getElementById("displayname");
    const emailInput = document.getElementById("email");

    const usernameError = document.getElementById("usernameError");
    const emailError = document.getElementById("emailError");

    let isUserNameValid = validateName(usernameInput);
    let isPasswordValid = validatePass(passwordInput);
    let isDisplaynameValid = validateDisplayName(displaynameInput);
    let isEmailValid = validateEmail(emailInput);

    // Kiểm tra username qua API
    if (isUserNameValid) {
        let isUsernameExists = await checkUsernameExists(usernameInput.value);
        if (isUsernameExists) {
            usernameError.style.display = "block";
            usernameError.textContent = "Tên tài khoản đã tồn tại, vui lòng nhập tên khác!";
            isUserNameValid = false;
        }
    }

    // Kiểm tra email qua API
    if (isEmailValid) {
        let isEmailExists = await checkEmailExists(emailInput.value);
        if (isEmailExists) {
            emailError.style.display = "block";
            emailError.textContent = "Email đã tồn tại, vui lòng nhập email khác!";
            isEmailValid = false;
        }
    }

    return isUserNameValid && isPasswordValid && isDisplaynameValid && isEmailValid;

}
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".create-form");

    form.addEventListener("submit", async function (event) {
        event.preventDefault(); // Ngăn form submit ngay lập tức
        const usernameInput = document.getElementById("username");
        const passwordInput = document.getElementById("password");
        const displaynameInput = document.getElementById("displayname");
        const emailInput = document.getElementById("email");

        const usernameError = document.getElementById("usernameError");
        const emailError = document.getElementById("emailError");

        let isUserNameValid = validateName(usernameInput);
        let isPasswordValid = validatePass(passwordInput);
        let isDisplaynameValid = validateDisplayName(displaynameInput);
        let isEmailValid = validateEmail(emailInput);

        // Kiểm tra username qua API
        if (isUserNameValid) {
            let isUsernameExists = await checkUsernameExists(usernameInput.value);
            if (isUsernameExists) {
                usernameError.style.display = "block";
                usernameError.textContent = "Tên tài khoản đã tồn tại, vui lòng nhập tên khác!";
                isUserNameValid = false;
            }
        }

        // Kiểm tra email qua API
        if (isEmailValid) {
            let isEmailExists = await checkEmailExists(emailInput.value);
            if (isEmailExists) {
                emailError.style.display = "block";
                emailError.textContent = "Email đã tồn tại, vui lòng nhập email khác!";
                isEmailValid = false;
            }
        }

        // Nếu có lỗi, không submit
        if (!(isUserNameValid && isPasswordValid && isDisplaynameValid && isEmailValid)) {
            return; // Dừng luôn, không gửi form
        }

        // Nếu hợp lệ, hiển thị thông báo và submit form
        Swal.fire({
            icon: "success",
            title: "Thành Công!",
            text: " Thông tin cá nhân đã được lưu thành công.",
            backdrop: false, // ❌ Tắt nền mờ phía sau
            confirmButtonText: "OK"
        }).then(() => {
            form.submit(); // Gửi form sau khi người dùng nhấn OK
        });
    });
});



