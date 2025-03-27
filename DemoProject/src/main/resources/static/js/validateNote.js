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
    const units = ["", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"];
    const teens = ["Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"];
    const tens = ["", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"];
    const thousands = ["", "Thousand", "Million", "Billion"];

    if (num == 0) return "Zero";
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
                word += units[Math.floor(n / 100)] + " Hundred ";
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
        let value = money.value.replace(/,/g, '');
        let moneyError = [];
        let notenameError = [];
        let noteImageError = [];
        money.value = money.value.replace(/,/g, '');
        if (notename.value.length < 5) {
            notenameError.push("Note name is equal or greater than 5 characters");
        }else if (notename.value.length > 255 ){
            notenameError.push("Note name is only up to 255 characters.");
        }

        if (money.value.trim() === "") {
            moneyError.push("Money cannot be empty");
        }
        else if (!/^\d+$/.test(value)) {
            moneyError.push("Money only contain number.");
        }else if (parseInt(money.value) > 2147483647) {
            moneyError.push("Money must be equal to the int type of data, be less than or equal to 2,147,483,647.");
        }

        if(noteImage.value.trim() === ""){
            noteImageError.push("Note image cannot be empty")
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
        }

    });
});