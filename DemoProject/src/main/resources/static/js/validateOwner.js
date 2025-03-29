var nameRegex = /^[a-zA-ZÀ-Ỹà-ỹ\s]+$/;
var phoneRegex = /^[0-9]{10,11}$/;
var moneyRegex = /^[0-9]+$/;
var today = new Date().toISOString().split("T")[0];
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".create-form");

    const username = document.getElementById("username");
    const password = document.getElementById("password");
    const displayname = document.getElementById("displayname");
    const email = document.getElementById("email");
    errorMessage.style.color = "red";
    errorMessage.style.fontWeight = "bold";

    form.addEventListener("submit", function (event) {

        event.preventDefault();
        let usernameError = [];
        let passwordError = [];
        let displaynameError = [];
        let emailError = [];



        // Kiểm tra name
        if (username.value.length < 5) {
            usernameError.push("Tên đầy đủ phải lớn hơn hoặc bằng 5 kí tự");
        }


        // Kiểm tra Address
        if (password.value.length < 5) {
            passwordError.push("Địa chỉ phải lớn hơn hoặc bằng 5 kí tự");
        }

        if (displayname.value === "") {
            displaynameError.push("TEEN HIEENR THIJ không được để trống.");
        }

        // Kiểm tra phone
        if (email.value.trim() === "") {
            emailError.push(" EAMIAL không được để trống");
        }

        // Nếu có lỗi, ngăn submit và hiển thị thông báo
        document.querySelectorAll(".error-msg").forEach(el => {
            el.innerText = "";
            el.style.display = "none"; // Ẩn tất cả trước khi kiểm tra lỗi mới
        });


// Gắn lỗi vào từng trường cụ thể
        function showError(fieldId, messages) {
            const errorElement = document.getElementById(fieldId + "Error");
            if (errorElement && messages.length > 0) {
                errorElement.innerText = messages.join("\n");
                errorElement.style.display = "block"; // Hiện lỗi khi có nội dung
            }
        }

        if (
            usernameError.length > 0 ||
            passwordError.length > 0 ||
            displaynameError.length > 0 ||
            emailError.length > 0
        ) {
            event.preventDefault();

            showError("username", usernameError);
            showError("password", passwordError);
            showError("displayname", displaynameError);
            showError("email", emailError);
        }

        else{
            Swal.fire({
                icon: "success",
                title: "Thành Công!",
                text: " Thông tin cá nhân đã được lưu thành công.",
                backdrop: false, // ❌ Tắt nền mờ phía sau
                confirmButtonText: "OK"
            }).then(() => {
                form.submit(); // Gửi form sau khi người dùng nhấn OK
            });
        }

    });
});
