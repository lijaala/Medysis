document.addEventListener("DOMContentLoaded", function () {
    // Fetch data for Admin Dashboard
    fetch('/api/dashboard/admin')
        .then(response => response.json())
        .then(data => {
            console.log("Fetched data:", data); // Debugging step
            if (data && data.chartData) {
                renderChart(data.chartData); // ✅ Use the correct key
            } else {
                console.error("chartData is missing in API response.");
            }
            if (data && data.calendarData) {
                renderCalendar(data.calendarData); // ✅ Add this line
            } else {
                console.error("calendarData is missing in API response.");
            }
        })
        .catch(error => console.error("Error fetching dashboard data:", error));


    // Function to render calendar
    function renderCalendar(calendarData) {
        const calendarGrid = document.getElementById('calendarGrid');
        const currentMonthSpan = document.getElementById('currentMonth');
        const prevMonthButton = document.getElementById('prevMonth');
        const nextMonthButton = document.getElementById('nextMonth');

        let currentDate = new Date();
        let currentMonth = currentDate.getMonth();
        let currentYear = currentDate.getFullYear();

        function updateCalendar(month, year) {
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            const daysInMonth = lastDay.getDate();
            const startDay = firstDay.getDay(); // 0 (Sunday) to 6 (Saturday)

            currentMonthSpan.textContent = new Date(year, month).toLocaleString('default', { month: 'long' }) + ' ' + year;
            calendarGrid.innerHTML = ''; // Clear previous grid

            // Add weekday headers
            const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            weekdays.forEach(weekday => {
                const dayHeader = document.createElement('div');
                dayHeader.classList.add('calendar-day');
                dayHeader.textContent = weekday;
                calendarGrid.appendChild(dayHeader);
            });

            let dayCounter = 1;
            for (let i = 0; i < 6; i++) { // Up to 6 weeks
                for (let j = 0; j < 7; j++) {
                    const dayDiv = document.createElement('div');
                    dayDiv.classList.add('calendar-day');

                    if (i === 0 && j < startDay) {
                        // Empty cells before the first day of the month
                    } else if (dayCounter > daysInMonth) {
                        // Empty cells after the last day of the month
                    } else {
                        dayDiv.textContent = dayCounter;
                        const date = new Date(year, month, dayCounter);
                        const dateStr = date.toISOString().slice(0, 10); // Format as YYYY-MM-DD
                        dayDiv.textContent = dayCounter; // Display the day number by default

                        // Find matching data for the current date
                        const dataForDate = calendarData.find(entry => entry && entry.length === 3 && entry[0] === dateStr);
                        if (dataForDate) {
                            const totalAppointments = dataForDate[1];
                            const urgentCount = dataForDate[2];
                            dayDiv.classList.add('has-appointments');
                            dayDiv.innerHTML = `<div>${dayCounter}</div><div>Total: ${totalAppointments}</div><div>Pending: ${urgentCount}</div>`;
                            dayDiv.title = `Total Appointments: ${totalAppointments} | Pending: ${urgentCount}`;
                        }
                        dayCounter++;
                    }
                    calendarGrid.appendChild(dayDiv);
                }
            }
        }

        updateCalendar(currentMonth, currentYear);

        prevMonthButton.addEventListener('click', () => {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            updateCalendar(currentMonth, currentYear);
        });

        nextMonthButton.addEventListener('click', () => {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            updateCalendar(currentMonth, currentYear);
        });
    }
    // Function to render the chart
    function renderChart(appointmentsByStaff) {
        const ctx = document.querySelector('.barchart').getContext('2d');

        const chartData = {
            labels: Object.keys(appointmentsByStaff),
            datasets: [{
                label: 'Appointments by Staff',
                data: Object.values(appointmentsByStaff),
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        };

        new Chart(ctx, {
            type: 'bar',
            data: chartData,
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});