function formatNumber(input) {
    // Lấy giá trị từ input và loại bỏ dấu phẩy
    let value = input.value.replace(/,/g, '');

    // Kiểm tra xem có phải số hợp lệ không
    if (!isNaN(value) && value !== "") {
        // Cập nhật input với giá trị có dấu phẩy (format số)
        input.value = Number(value).toLocaleString('en-US');

        // Hiển thị cách đọc số bằng chữ
        document.getElementById("numberInWords").innerText = numberToWords(value) + " VND";
    } else {
        document.getElementById("numberInWords").innerText = "";
    }
}

function numberToWords(num) {
    const units = ["", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín"];
    const teens = ["Mười một", "Mười hai", "Mười ba", "Mười bốn", "Mười lăm", "Mười sáu", "Mười bảy", "Mười tám", "Mười chín"];
    const tens = ["", "Mười", "Hai mươi", "Ba mươi", "Bốn mươi", "Năm mươi", "Sáu mươi", "Bảy mươi", "Tám mươi", "Chín mươi"];
    const thousands = ["", "Ngàn", "Triệu", "Tỷ"];

    if (/\D/.test(num.toString().replace(/,/g, ''))) {
        return "";
    }

    if (num == 0) return "Không";
    let words = "";
    let numStr = num.toString();
    let numArr = [];

    // Chia số thành nhóm ba chữ số
    while (numStr.length > 0) {
        numArr.unshift(numStr.slice(-3));
        numStr = numStr.slice(0, -3);
    }

    numArr.forEach((part, index) => {
        if (part != "000") {
            let word = "";
            let n = parseInt(part);

            if (n >= 100) {
                word += units[Math.floor(n / 100)] + " Trăm ";
                n %= 100;
            }

            if (n >= 11 && n <= 19) {
                word += teens[n - 11] + " ";
            } else {
                word += tens[Math.floor(n / 10)] + " ";
                word += units[n % 10] + " ";
            }

            if (thousands[numArr.length - 1 - index]) {
                word += thousands[numArr.length - 1 - index] + " ";
            }

            words += word;
        }
    });

    return words.trim();
}
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".profile-form");

    const notename = document.getElementById("notename");
    const money = document.getElementById("money");
    const noteImage = document.getElementById("noteImage");

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        let value = money.value.replace(/,/g, '');
        let moneyError = [];
        let notenameError = [];
        let noteImageError = [];


        money.value = money.value.replace(/,/g, '');
        if (notename.value.length < 5) {
            notenameError.push("Tên giấy nợ phải lớn hơn hoặc bằng 5 kí tự");
        }else if (notename.value.length > 255 ){
            notenameError.push("Tên giấy nợ tối đa 255 kí tự.");
        }

        if (money.value.trim() === "") {
            moneyError.push("Tiền không được để trống");
        }
        else if (!/^\d+$/.test(value)) {
            moneyError.push("Tiền chỉ được chứa số.");
        }else if (parseInt(money.value) > 2147483647) {
            moneyError.push("Tiền phải bằng kiểu dữ liệu int, nhỏ hơn hoặc bằng 2,147,483,647.");
        }


        // Kiểm tra Note Image
        const file = noteImage.files[0];
        const fileSizeMB = file ? file.size / (1024 * 1024) : 0;
        if (noteImage.value.trim() === "") {
            noteImageError.push("Lưu ý hình ảnh không được để trống khi trạng thái tiền không phải là số không.");
        } else if (fileSizeMB > 10) {
            noteImageError.push("Ảnh không được vượt quá 10MB.");
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
            notenameError.length > 0 ||
            moneyError.length > 0 ||
            noteImageError.length > 0
        ) {
            event.preventDefault();
            showError("notename", notenameError);
            showError("money",moneyError)
            showError("noteImage", noteImageError);
        } else{
            Swal.fire({
                title: "Thành Công!",
                text: "Tạo giấy nợ thành công",
                icon: "success",
                draggable: true
            }).then(() => {
                event.target.submit();
            });
            // Hiển thị thông báo bằng alert
        }

    });
});