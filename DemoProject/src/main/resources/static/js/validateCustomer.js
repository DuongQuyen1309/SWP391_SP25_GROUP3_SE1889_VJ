document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("moneyState").value = "0";
});
    function formatNumber(input) {
    let moneyState = document.getElementById("moneyState").value.replace(/,/g, '');
    let kindOfNote = document.getElementById("kindOfNote");
    let kindOfNoteLabel = document.getElementById("kindOfNoteLabel");
    let noteName = document.getElementById("noteName");
    let noteNameLabel = document.getElementById("noteNameLabel");
    let noteImageLabel = document.getElementById("noteImageLabel");
    let noteImage = document.getElementById("noteImage");

    if (moneyState !== "0" && moneyState.trim() !== "") {
    kindOfNote.style.display = "block";
    kindOfNoteLabel.style.display = "block";
    noteName.style.display = "block";
    noteNameLabel.style.display = "block";
    noteImage.style.display = "block";
    noteImageLabel.style.display = "block";
} else {
    kindOfNote.style.display = "none";
    kindOfNoteLabel.style.display = "none";
    noteName.style.display = "none";
    noteNameLabel.style.display = "none";
    noteImage.style.display = "none";
    noteImageLabel.style.display = "none";
}

    let value = input.value.replace(/,/g, '');

    if (!isNaN(value) && value !== "") {
    input.value = Number(value).toLocaleString('en-US');

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

    // var existingPhones = /*[[${phoneList}]]*/ [];

    var nameRegex = /^[a-zA-ZÀ-Ỹà-ỹ\s]+$/;
    var phoneRegex = /^[0-9]{10,11}$/;
    var moneyRegex = /^[0-9]+$/;
    var today = new Date().toISOString().split("T")[0];
    document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".profile-form");

    const name = document.getElementById("name");
    const address = document.getElementById("address");
    const dob = document.getElementById("dob");
    const phone = document.getElementById("phone");
    const noteName = document.getElementById("noteName");
    const moneyState = document.getElementById("moneyState");
    const noteImage = document.getElementById("noteImage");
    const errorMessage = document.createElement("p");
    errorMessage.style.color = "red";
    errorMessage.style.fontWeight = "bold";

    form.addEventListener("submit", function (event) {
    let nameError = [];
    let addressError = [];
    let dobError = [];
    let phoneError = [];
    let moneyNoteError = [];
    let noteNameError = [];
    let imageError = [];



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
    } else if(existingPhones.includes(phone.value)){
        phoneError.push("Phone cannot be duplicated");
    }

    let value = moneyState.value.replace(/,/g, '');
    moneyState.value = moneyState.value.replace(/,/g, '');

    moneyState.value = value;  // Cập nhật lại giá trị đã loại bỏ dấu phẩy
    // Chuyển đổi giá trị moneyState thành số nguyên
    let moneyNumber = parseInt(value, 10);

    if (moneyState.value.trim() !== "" && moneyNumber !== 0 && noteName.value.length < 5) {
        noteNameError.push("Note name is equal or greater than 5 characters");
    }

    if (moneyState.value.trim() === "") {
        moneyNoteError.push("Money cannot be empty");
    } else if (!/^\d+$/.test(value)) {
        moneyNoteError.push("Money only contain number.");
    }else if (parseInt(value) > 2147483647) {
        moneyNoteError.push("Money must be equal to the int type of data, be less than or equal to 2,147,483,647.");
    }

    if(moneyState.value.trim() !== "" && moneyNumber !== 0 && noteImage.value.trim() === ""){
        imageError.push("Note image cannot be empty");
    }

        // Kiểm tra Note Image
        const file = noteImage.files[0];
        const fileSizeMB = file ? file.size / (1024 * 1024) : 0;
        if (moneyState.value.trim() !== "" && moneyNumber !== 0 && noteImage.value.trim() === "") {
            imageError.push("Note image cannot be empty when money state is not zero.");
        } else if (fileSizeMB > 10) {
            imageError.push("Note image cannot exceed 10MB.");
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
            nameError.length > 0 ||
            addressError.length > 0 ||
            dobError.length > 0 ||
            phoneError.length > 0 ||
            noteNameError.length > 0 ||
            moneyNoteError.length > 0 ||
            imageError.length > 0
        ) {
            event.preventDefault();

            showError("name", nameError);
            showError("address", addressError);
            showError("dob", dobError);
            showError("phone", phoneError);
            showError("moneyState", moneyNoteError);
            showError("noteName", noteNameError);
            showError("noteImage", imageError);
        }

});
});
