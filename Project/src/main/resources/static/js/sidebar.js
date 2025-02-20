document.addEventListener('DOMContentLoaded', () => {
    const menuItems = document.querySelectorAll('.menu li');
    const sections = document.querySelectorAll('.section');

    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            // Remove 'active' class from all menu items
            menuItems.forEach(menuItem => menuItem.classList.remove('active'));

            // Add 'active' class to the clicked menu item
            item.classList.add('active');

            // Hide all sections
            sections.forEach(section => section.style.display = 'none');

            // Display the corresponding section
            const target = item.getAttribute('data-target');
            const activeSection = document.getElementById(target);
            if (activeSection) {
                activeSection.style.display = 'flex';

            }
        });
    });
});


