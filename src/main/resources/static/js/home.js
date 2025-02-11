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

    ['registerMenu', 'listMenu'].forEach(menuId => {
        const menu = document.getElementById(menuId);
        const arrow = document.querySelector(`[onclick="toggleSubmenu('${menuId}')"] .arrow`);

        if (localStorage.getItem(menuId) === 'visible') {
            menu.style.display = 'block';
            arrow.style.transform = 'rotate(90deg)';
        }
    });
});