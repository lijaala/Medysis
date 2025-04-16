document.addEventListener('DOMContentLoaded', () => {
    const menuItems = document.querySelectorAll('.menu li');
    const sections = document.querySelectorAll('.section');
    const userRole = document.querySelector('.menu').getAttribute('data-user-role'); // Get user role from the menu

    // Function to activate a specific section
    const activateSection = (target) => {
        menuItems.forEach(menuItem => menuItem.classList.remove('active'));
        sections.forEach(section => section.style.display = 'none');

        const activeItem = document.querySelector(`.menu li[data-target="${target}"]`);
        const activeSection = document.getElementById(target);

        if (activeItem && activeSection) {
            activeItem.classList.add('active');
            activeSection.style.display = 'flex';

        }
        const searchInput = document.getElementById('search');

        if (target === 'patients') {
            if (searchInput) {
                searchInput.addEventListener('input', () => {
                    filterPatients(searchInput.value);
                });
                filterPatients(searchInput.value);
            }
        } else if (target === 'appointments') {
            if (searchInput) {
                searchInput.addEventListener('input', () => {
                    searchAppointments(searchInput.value);
                });
                filterAppointments(searchInput.value); // Initial call for appointments
            }
        }
        else if (target === 'staff') {
            if (searchInput) {
                searchInput.addEventListener('input', () => {
                    filterStaff(searchInput.value);
                });
                filterStaff(searchInput.value); // Initial staff load
            }
        }
        else if(target==='testRequests'){
            if(searchInput){
                searchInput.addEventListener('input', () => {
                    searchLabOrders(searchInput.value);
                });
                searchLabOrders(searchInput.value);
            }
        }

    };
    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            const target = item.getAttribute('data-target');
            activateSection(target);
        });
    });

    // Determine the default active section based on the user role
    let defaultTarget = '';
    if (userRole === 'ROLE_ADMIN') {
        defaultTarget = 'adminDashboard';
    } else if (userRole === 'ROLE_DOCTOR') {
        defaultTarget = 'doctorDashboard';
    } else if (userRole === 'ROLE_LAB TECHNICIAN') {
        defaultTarget = 'labDashboard';
    }

    // Activate the default section
    if (defaultTarget) {
        activateSection(defaultTarget);
    }
});