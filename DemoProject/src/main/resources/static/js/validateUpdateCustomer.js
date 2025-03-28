// var existingPhones = /*[[${phoneList}]]*/ [];
var nameRegex = /^[a-zA-ZÀ-Ỹà-ỹ\s]+$/;
var phoneRegex = /^[0-9]{10,11}$/;
var today = new Date().toISOString().split("T")[0];
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".profile-form");

    const name = document.getElementById("name");
    const address = document.getElementById("address");
    const dob = document.getElementById("dob");
    const phone = document.getElementById("phone");

    form.addEventListener("submit", function (event) {
        let nameError = [];
        let addressError = [];
        let dobError = [];
        let phoneError = [];

        // Kiểm tra name
        if (name.value.length < 5) {
            nameError.push("Full Name is equal or greater than 5 characters");
        } else if (!name.value.match(nameRegex)) {
            nameError.push("Full Name cannot contain numbers or special characters.");
        } else if (name.value.length > 50) {
            nameError.push("Full Name is only up to 50 characters.");
        }


        // Kiểm tra Address
        if (address.value.length < 5) {
            addressError.push("Address is equal or greater than 5 characters");
        }else if (address.value.length > 255) {
            addressError.push("Address is only up to 255 characters.");
        }

        if (dob.value === "") {
            dobError.push("Date of Birth cannot be empty.");
        }else if (dob.value > today) {
            dobError.push("Date of Birth cannot be a future date.");
        }

        // Kiểm tra phone
        if (phone.value.trim() === "") {
            phoneError.push("Phone cannot be empty");
        } else if (!phone.value.match(phoneRegex)) {
            phoneError.push("Phone number must be between 10 and 11 digits and contain only numbers.");
        } else if(existingPhones.includes(phone.value) && (pre_phone !== phone.value)){
            phoneError.push("Phone cannot be duplicated");
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

        if (nameError.length > 0 ||
            addressError.length > 0 ||
            dobError.length > 0 ||
            phoneError.length > 0 ) {
            event.preventDefault();

            showError("name", nameError);
            showError("address", addressError);
            showError("dob", dobError);
            showError("phone", phoneError);
        }

    });
});
