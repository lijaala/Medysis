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
            if (data && data.totalPatients !== undefined) {
                document.getElementById('totalPatients').textContent = data.totalPatients;
            } else {
                console.error("totalPatients is missing in API response.");
            }

            if (data && data.newPatients !== undefined) {
                document.getElementById('newPatients').textContent = data.newPatients;
            } else {
                console.error("newPatients is missing in API response.");
            }

            if (data && data.totalAppointments !== undefined) {
                document.getElementById('totalAppointments').textContent = data.totalAppointments;
            } else {
                console.error("totalAppointments is missing in API response.");
            }
        })
        .catch(error => console.error("Error fetching dashboard data:", error));


    // Function to render calendar
    function renderCalendar(calendarData) {
        const calendarGrid = document.getElementById('calendarGrid');
        const currentMonthSpan = document.getElementById('currentMonth');
        const prevMonthButton = document.getElementById('prevMonth');
        const nextMonthButton = document.getElementById('nextMonth');
        const eventDetails = document.createElement('div'); // Create a div for event details
        eventDetails.classList.add('event-details'); // Add a class for styling
        document.querySelector('.calendar').appendChild(eventDetails); // Append it to the calendar container




        let currentDate = new Date();
        let currentMonth = currentDate.getMonth();
        let currentYear = currentDate.getFullYear();
        let today = currentDate.getDate();  // Get today's date
        let selectedDate=null;

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
            let todayHighlighted = false; // Flag to track if today is already highlighted

            for (let i = 0; i < 6; i++) { // Up to 6 weeks
                for (let j = 0; j < 7; j++) {
                    const dayDiv = document.createElement('div');
                    dayDiv.classList.add('calendar-day');

                    if (i === 0 && j < startDay) {

                    }
                    else if (dayCounter > daysInMonth) {
                        // Empty cells after the last day of the month
                    } else {
                        const date = new Date(year, month, dayCounter);
                        const dateStr = date.toISOString().slice(0, 10); // Format as YYYY-MM-DD
                        dayDiv.textContent = dayCounter; // Display the day number by default


                        if (dayCounter === today && month === currentDate.getMonth() && year === currentDate.getFullYear() && !todayHighlighted) {
                            dayDiv.classList.add('today'); // Highlight today
                            selectedDate = dateStr; // Set today as initially selected
                            displayEvents(dateStr); // Display events for today by default
                        }

                        const dataForDate = calendarData.find(entry => entry && entry.length === 3 && entry[0] === dateStr);

                        dayDiv.addEventListener('click', () => {
                            // Remove 'selected' class from all days
                            const allDayDivs = calendarGrid.querySelectorAll('.calendar-day');
                            allDayDivs.forEach(div => div.classList.remove('selected'));

                            dayDiv.classList.add('selected'); // Add 'selected' class to clicked day
                            selectedDate = dateStr; // Update selected date

                            if (dataForDate) {
                                const totalAppointments = dataForDate[1];
                                const urgentCount = dataForDate[2];
                                displayEvents(dateStr, totalAppointments, urgentCount);
                            } else {
                                displayEvents(dateStr);

                            }
                        });
                        dayCounter++;
                    }
                    calendarGrid.appendChild(dayDiv);
                }
            }
        }
        function displayEvents(dateStr, totalAppointments = 0, urgentCount = 0) {
            eventDetails.innerHTML = ""; // Clear previous details

            if (totalAppointments > 0) {
                eventDetails.innerHTML = `
                    <h3>Appointments for ${dateStr}</h3>
                    <p>Total: ${totalAppointments}</p>
                    <p>Pending: ${urgentCount}</p>
                `;
            } else {
                eventDetails.innerHTML = '<h3>No events for this day</h3>';
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