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
        event.preventDefault();
        let nameError = [];
        let addressError = [];
        let dobError = [];
        let phoneError = [];

        // Kiểm tra name
        if (name.value.length < 5) {
            nameError.push("Tên đầy đủ phải lớn hơn hoặc bằng 5 kí tự");
        } else if (!name.value.match(nameRegex)) {
            nameError.push("Tên đầy đủ không được chứa số hoặc kí tự đặc biệt.");
        } else if (name.value.length > 50) {
            nameError.push("Tên đầy đủ chỉ có 50 chữ cái.");
        }


        // Kiểm tra Address
        if (address.value.length < 5) {
            addressError.push("Địa chỉ phải lớn hơn hoặc bằng 5 kí tự");
        }else if (address.value.length > 255) {
            addressError.push("Địa chỉ chỉ có 255 chữ cái.");
        }
        if (dob.value === "") {
            dobError.push("Ngày sinh không được để trống.");
        }else if (dob.value > today) {
            dobError.push("Ngày sinh phải là ngày trong quá khứ.");
        }

        // Kiểm tra phone
        if (phone.value.trim() === "") {
            phoneError.push(" Số điện thoại không được để trống");
        } else if (!phone.value.match(phoneRegex)) {
            phoneError.push("Số điện thoại phải là 10 hoặc 11 số và chỉ được là số");
        } else if(existingPhones.includes(phone.value)){
            phoneError.push("Số điện thoại không được trùng lặp");
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

        else{
            Swal.fire({
                title: "Bạn có muốn chỉnh sửa khách hàng không?",
                text: "Bạn có chắc không",
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#3085d6",
                cancelButtonColor: "#d33",
                cancelButtonText: "Hủy",
                confirmButtonText: "Sửa"
            }).then((result) => {
                if (result.isConfirmed) {
                    document.querySelector(".profile-form").submit(); // ✅ Submit form thủ công
                }
            });
        }

    });
});
