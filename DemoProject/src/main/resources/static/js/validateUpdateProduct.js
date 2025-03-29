
function validateName(input) {
    const nameRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s]+$/;
    const errorElement = document.getElementById("nameError");
    const minlength = 3;
    const maxlength = 50;
    if (input.value.trim().length < 3 || input.value.trim().length > 50) {
        errorElement.style.display = "block";
        errorElement.textContent = "Tên sản phẩm ít nhất 3 kí tự và ít hơn 50 kí tự!"
        return false;
    } else {
        if (!nameRegex.test(input.value)) {
            errorElement.style.display = "block";
            errorElement.textContent = "Tên sản phẩm có chứa kí tự không hợp lệ!"
        }
        errorElement.style.display = "none";
        return true;
    }
}

function validatePrice(input) {
    const errorElement = document.getElementById("priceError");
    const priceRegex = /^[0-9]+(\.[0-9]{1,2})?$/; // Matches numbers, optionally with up to 2 decimal places.

    if (!priceRegex.test(input.value) || parseFloat(input.value) <= 0) {
        errorElement.style.display = "block";
        errorElement.textContent = "Giá phải là số tự nhiên lớn 0!";
        return false;
    } else {
        errorElement.style.display = "none";
        return true;
    }
}

function validateDescription(input) {
    const errorElement = document.getElementById("descriptionError");
    const descriptionRegex = /^[a-zA-Z0-9\u00C0-\u1EF9\s.,]*$/;
    const maxLength = 500;
    const inputValue = input.value.trim();

    if (inputValue.length > maxLength) {
        errorElement.style.display = "block";
        errorElement.textContent = `Mô tả quá dài!`;
        return false;
    }
    else if (!descriptionRegex.test(inputValue)) {
        errorElement.style.display = "block";
        errorElement.textContent = "Mô tả chứa kí tự không hợp lệ!";
        return false;
    }
    else {
        errorElement.style.display = "none";
        return true;
    }
}

async function checkProductName(name,id) {
    const response = await fetch(`/product/checknameandid?name=${encodeURIComponent(name)}&id=${id}`);
    if (!response.ok) {
        throw new Error("Đã xảy ra lỗi trong quá trình kiểm tra tên sản phẩm");
    }
    return await response.json();
}


async function validateForm(event) {
    event.preventDefault();
    const nameInput = document.getElementById("name");
    const priceInput = document.getElementById("price");
    const descriptionInput = document.getElementById("description");

    const isNameValid = validateName(nameInput);
    const isPriceValid = validatePrice(priceInput);
    const isDescriptionValid = validateDescription(descriptionInput);

    const nameError = document.getElementById("nameError");
    if (!isNameValid || !isPriceValid || !isDescriptionValid) {
        return false;
    }

    const preName = document.getElementById("preName").value;
    const currentId = document.getElementById("currentId").value;
    let isNameExists = false;
    try {
        isNameExists = await checkProductName(nameInput.value, currentId);
        if (isNameExists && nameInput.value !== preName) {
            console.log("da vao day");
            nameError.style.display = "block";
            nameError.textContent = "Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.";
            return false;
        }
    } catch (error) {
        alert(error.message);
        return false
    }
    if (isNameValid && isPriceValid && isDescriptionValid && isNameExists) {
        Swal.fire({
            title: 'Xác nhận sửa sản phẩm?',
            text: 'Bạn có chắc chắn muốn sửa sản phẩm với thông tin này?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sửa sản phẩm',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                event.target.submit();
            }
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const productForm = document.getElementById("productValid");
    productForm.addEventListener("submit", (event) => validateForm(event));
});



