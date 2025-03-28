document.addEventListener("DOMContentLoaded", function () {
    let uploadButton = document.getElementById('uploadAvatarBtn');
    let fileInput = document.getElementById('avatarInput');
    let avatarPreview = document.querySelector('.user-avatar'); // Lấy thẻ <img> hiển thị avatar
    let avatarLarge= document.querySelector('.user-avatar-large');

    if (!uploadButton || !fileInput || !avatarPreview) {
        console.error("Upload button, file input, or avatar image không được tìm tấy.");
        return;
    }

    // Khi nhấn nút Upload Image, mở hộp thoại chọn ảnh
    uploadButton.addEventListener('click', function () {
        fileInput.click();
    });

    // Khi người dùng chọn ảnh
    fileInput.addEventListener('change', function (event) {
        let file = event.target.files[0];
        if (file) {
            console.log("File selected:", file.name);

            let formData = new FormData();
            formData.append("avatar", file);

            fetch("/user/upload-avatar", {
                method: "POST",
                body: formData
            })
                .then(response => response.json()) // Chuyển đổi phản hồi thành JSON
                .then(data => {
                    if (data.success === "true") {
                        console.log("Avatar được tải lên thành công:", data.imageUrl);

                        // Cập nhật ảnh ngay lập tức trên giao diện
                        avatarPreview.src = data.imageUrl + "?t=" + new Date().getTime();
                        avatarLarge.src = data.imageUrl + "?t=" + new Date().getTime();

                        Swal.fire({
                            icon: "success",
                            title: "Tải avatar thành công!",
                            text: "Avatar đã được lưu thành công.",
                            backdrop: false, // ❌ Tắt nền mờ phía sau
                            confirmButtonText: "OK"
                        })
                    } else {
                        alert("Error: " + data.message);
                    }
                })
                .catch(error => {
                    console.error("Error uploading avatar:", error);
                    alert("Tải avatar thất bại.");
                });
        } else {
            console.log("No file selected");
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".profile-form");
    const name = document.getElementById("name");
    const phone = document.getElementById("phone");
    const dob = document.getElementById("dob");
    const address = document.getElementById("address");




    async function checkPhoneExists(phone) {
        try {
            let response = await fetch(`/api/check-phone-account?phone=${phone}`);
            if (!response.ok) throw new Error("Error when checking phone");
            let data = await response.json();
            return data.exists;
        } catch (error) {
            console.error("Error when checking phone:", error);
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
            document.getElementById("phone-error").textContent = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số.";
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


        const dobDate = new Date(dob.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (dobDate >= today) {
            document.getElementById("dob-error").textContent = "Ngày sinh phải trong quá khứ.";
            document.getElementById("dob-error").style.display = "block";
            hasError = true;
        }


        if (hasError) {
            event.preventDefault();
            return;
        } else {
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

// window.onload = function() {
//     let alertMessage = "[[${alertMessage}]]";
//     if (alertMessage && alertMessage !== "null") {
//         alert(alertMessage);
//     }
// };


function enableEditing() {
    document.querySelectorAll('.profile-form input, .profile-form select').forEach(element => element.disabled = false);

    document.getElementById('editBtn').style.display = 'none';
    document.getElementById('cancelBtn').style.display = 'inline-block';
    document.getElementById('applyBtn').style.display = 'inline-block';

    // Lưu giá trị gốc để có thể hoàn tác nếu bấm Cancel
    sessionStorage.setItem('originalData', JSON.stringify({
        name: document.getElementById('name').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value,
        dob: document.getElementById('dob').value,
        gender: document.getElementById('gender').value
    }));
}

function cancelEditing() {
    let originalData = JSON.parse(sessionStorage.getItem('originalData'));
    document.querySelectorAll('.error-message').forEach(el => el.textContent = "");


    document.getElementById('name').value = originalData.name;
    document.getElementById('phone').value = originalData.phone;
    document.getElementById('address').value = originalData.address;
    document.getElementById('dob').value = originalData.dob;
    document.getElementById('gender').value = originalData.gender;

    document.querySelectorAll('.profile-form input, .profile-form select').forEach(element => element.disabled = true);


    document.getElementById('editBtn').style.display = 'inline-block';
    document.getElementById('cancelBtn').style.display = 'none';
    document.getElementById('applyBtn').style.display = 'none';
    document.getElementById('error-message')?.remove();
}