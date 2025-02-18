function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');

    if (sidebar.classList.contains('hide')) {
        sidebar.classList.remove('hide');
        content.classList.remove('full-width');
        content.classList.add('shift');
        localStorage.setItem('sidebarState', 'open');
    } else {
        sidebar.classList.add('hide');
        content.classList.remove('shift');
        content.classList.add('full-width');
        localStorage.setItem('sidebarState', 'closed');
    }
}

function toggleSubmenu(menuId) {
    const menu = document.getElementById(menuId);
    const arrow = menu.previousElementSibling.querySelector('.arrow');
    const isVisible = menu.style.display === 'block';

    menu.style.display = isVisible ? 'none' : 'block';
    arrow.style.transform = isVisible ? 'rotate(0deg)' : 'rotate(90deg)';

    localStorage.setItem(menuId, isVisible ? 'hidden' : 'visible');
}

document.addEventListener("DOMContentLoaded", function () {
    if (localStorage.getItem('sidebarState') === 'closed') {
        document.getElementById('sidebar').classList.add('hide');
        document.getElementById('content').classList.add('full-width');
    }

    ['riceMenu', 'inventoryMenu'].forEach(menuId => {
        const menu = document.getElementById(menuId);
        const arrow = document.querySelector(`[onclick="toggleSubmenu('${menuId}')"] .arrow`);

        if (localStorage.getItem(menuId) === 'visible') {
            menu.style.display = 'block';
            arrow.style.transform = 'rotate(90deg)';
        }
    });
});

function showSuccessAlert() {
    alert("You are successfully registered!");
}

function openForm() {
    document.getElementById("studentFormModal").style.display = "block";
}

function closeForm() {
    document.getElementById("studentFormModal").style.display = "none";
}

// Đóng modal khi nhấn ra ngoài
window.onclick = function(event) {
    const modal = document.getElementById("studentFormModal");
    if (event.target === modal) {
        modal.style.display = "none";
    }
};

document.addEventListener("DOMContentLoaded", function() {
    document.getElementById("logout").addEventListener("click", function() {
        event.preventDefault();
        fetch("/api/logout", {
            method: "POST",
            credentials: "include"
        }).then(response => {
            if (response.ok) {
                alert("Đã đăng xuất!");
                window.location.href = "/login";
            }
        });
    });
});

function toggleDropdown() {
    document.getElementById("dropdown-menu").classList.toggle("show");
}

// Ẩn dropdown khi nhấn ra ngoài
window.onclick = function(event) {
    if (!event.target.matches('.user-name')) {
        let dropdowns = document.getElementsByClassName("dropdown-content");
        for (let i = 0; i < dropdowns.length; i++) {
            let openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}