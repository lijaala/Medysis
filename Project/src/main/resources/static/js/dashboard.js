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
// Fetch Doctor Dashboard Data
    fetch('/api/dashboard/doctor')
        .then(response => response.json())
        .then(data => {
            console.log("Fetched Doctor Dashboard Data:", data);

            const staticChartData = {
                "18-35 Years": 45,
                "35-45 Years": 30,
                "45-60 Years": 20,
                "60+ Years": 5
            };// Debugging

            if (data.totalAppointments !== undefined) {
                document.getElementById('totalAppointmentsDoc').textContent = data.totalAppointments;
            } else {
                console.error("totalAppointments is missing in API response.");
            }

            if (data.appointmentsToday !== undefined) {
                document.getElementById('appToday').textContent = data.appointmentsToday;
            } else {
                console.error("appointmentsToday is missing in API response.");
            }
            if (data.ageGroupDistribution) {
                renderAgeGroupBubbleChart(data.ageGroupDistribution, 'doctorAgeGroupChart');
            } else {
                console.error("ageGroupDistribution data is missing in the API response.");
            }


            let doctorCalendarData = [];
            if (data.appointmentsPerDay) {
                doctorCalendarData = Object.entries(data.appointmentsPerDay).map(([date, appointments]) => [date, appointments, 0]);
            }

            // Correctly call the doctor's render function
            if (doctorCalendarData.length > 0) {
                renderDoctorCalendar(  // Call the correct function
                    doctorCalendarData,
                    'calendarGridDoc',
                    'currentMonthDoc',
                    'prevMonthDoc',
                    'nextMonthDoc',
                    '.calendar.doctor-calendar .event-details'
                );
            } else {
                console.warn("No appointments data to display.");
                const calendarGrid = document.getElementById('calendarGridDoc'); // Use the doctor's ID
                if (calendarGrid) {
                    calendarGrid.innerHTML = "<p>No appointments scheduled.</p>";
                }
            }

        })
        .catch(error => console.error("Error fetching Doctor dashboard data:", error));

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


    function renderDoctorCalendar(calendarData, calendarGridId, currentMonthSpanId, prevMonthButtonId, nextMonthButtonId, eventDetailsContainerSelector) {
        const calendarGrid = document.getElementById(calendarGridId);
        const currentMonthSpan = document.getElementById(currentMonthSpanId);
        const prevMonthButton = document.getElementById(prevMonthButtonId);
        const nextMonthButton = document.getElementById(nextMonthButtonId);
        let eventDetails = document.querySelector(eventDetailsContainerSelector);
        if (!eventDetails) {
            eventDetails = document.createElement('div');
            eventDetails.classList.add('event-details');
            document.querySelector(eventDetailsContainerSelector.split(" ")[0]).appendChild(eventDetails);
        }

        let currentDate = new Date(); // Correctly scoped within renderCalendar
        let currentMonth = currentDate.getMonth();
        let currentYear = currentDate.getFullYear();
        let today = currentDate.getDate();
        let selectedDate = null;

        updateCalendar(currentMonth, currentYear); // Initial call

        function updateCalendar(month, year) {
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            const daysInMonth = lastDay.getDate();
            const startDay = firstDay.getDay();

            currentMonthSpan.textContent = new Date(year, month).toLocaleString('default', { month: 'long' }) + ' ' + year;
            calendarGrid.innerHTML = ''; // Clear the grid!

            const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            weekdays.forEach(weekday => {
                const dayHeader = document.createElement('div');
                dayHeader.classList.add('calendar-day', 'weekday-header');
                dayHeader.textContent = weekday;
                calendarGrid.appendChild(dayHeader);
            });

            let dayCounter = 1;
            let todayHighlighted = false;

            for (let i = 0; i < 6; i++) {
                for (let j = 0; j < 7; j++) {
                    const dayDiv = document.createElement('div');
                    dayDiv.classList.add('calendar-day');

                    if (i === 0 && j < startDay) {
                        // Empty cells
                    } else if (dayCounter > daysInMonth) {
                        // Empty cells
                    } else {
                        const date = new Date(year, month, dayCounter);
                        const dateStr = date.toISOString().slice(0, 10);
                        dayDiv.textContent = dayCounter;

                        if (dayCounter === today && month === currentDate.getMonth() && year === currentDate.getFullYear()) {
                            dayDiv.classList.add('today', 'selected');
                            selectedDate = dateStr;
                            todayHighlighted = true;
                            displayEvents(dateStr);
                        }

                        const dataForDate = calendarData.find(entry => entry && entry.length === 3 && entry[0] === dateStr);
                        dayDiv.addEventListener('click', () => {
                            const allDayDivs = calendarGrid.querySelectorAll('.calendar-day');
                            allDayDivs.forEach(div => div.classList.remove('selected'));

                            dayDiv.classList.add('selected');
                            selectedDate = dateStr;

                            if (dataForDate) {
                                const totalAppointments = dataForDate[1];
                                const urgentCount = dataForDate[2];
                                displayEvents(dateStr, totalAppointments);
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

        function displayEvents(dateStr, totalAppointments = 0) {
            eventDetails.innerHTML = "";
            if (totalAppointments > 0) {
                eventDetails.innerHTML = `
                    <h3>Appointments for ${dateStr}</h3>
                    <p>Total: ${totalAppointments}</p>              
                `;
            } else {
                eventDetails.innerHTML = '<h3>No events for this day</h3>';
            }
        }
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
    function renderAgeGroupBubbleChart(ageGroupData, canvasId) {
        const ctx = document.getElementById(canvasId).getContext('2d');

        const labels = Object.keys(ageGroupData);
        const percentages = Object.values(ageGroupData);

        const dataPoints = labels.map((label, index) => ({
            x: index,
            y: 1,
            r: percentages[index] * 10 // Adjust scaling factor as needed
        }));

        const colors = generateColors(labels.length);

        const chart = new Chart(ctx, { // Store the chart instance
            type: 'bubble',
            data: {
                datasets: [{
                    label: 'Age Group Distribution',
                    data: dataPoints,
                    backgroundColor: colors,
                    borderColor: '#fff',
                    borderWidth: 1,
                    hoverRadius: 10
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        type: 'linear',
                        title: {
                            display: true,
                            text: 'Age Group'
                        },
                        ticks: {
                            callback: function (value, index, values) {
                                return labels[value];
                            }
                        }
                    },
                    y: {
                        type: 'linear',
                        display: false
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const label = labels[context.dataIndex];
                                const percentage = percentages[context.dataIndex];
                                return `${label}: ${percentage}%`;
                            }
                        }
                    }
                }
            }
        });

        // Plugin to draw labels inside bubbles (after chart is drawn)
        Chart.register({
            id: 'bubbleLabelPlugin',
            afterDraw: chart => {
                const data = chart.data.datasets[0].data;
                const meta = chart.getDatasetMeta(0);

                ctx.font = '12px Arial'; // Adjust font size
                ctx.fillStyle = 'black'; // Adjust color
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';

                data.forEach((dataPoint, index) => {
                    const arc = meta.data[index];
                    const centerX = arc.x;
                    const centerY = arc.y;
                    const radius = arc.r;

                    if (centerX && centerY && radius > 0) { // Check for valid coordinates
                        const label = labels[index];
                        const percentage = percentages[index];
                        const verticalOffset = 0; // Adjust vertical offset if needed
                        ctx.fillText(`${label}\n${percentage}%`, centerX, centerY + verticalOffset);
                    }
                });
            }
        });
    }
    // Function to generate distinct colors
    function generateColors(numColors) {
        const colors = [];
        for (let i = 0; i < numColors; i++) {
            const hue = (i * 360) / numColors; // Distribute hues evenly
            colors.push(`hsl(${hue}, 70%, 60%)`); // Adjust saturation and lightness as needed
        }
        return colors;
    }

//Lab Tech stats
    fetch('/api/dashboard/lab-tech')
        .then(response => response.json())
        .then(data => {
            console.log("Fetched data:", data); // Debugging step
            if (data.totalLabTests != undefined) {
                document.getElementById('totalTests').textContent = data.totalLabTests;

            } else {
                console.error("Total Tests is missing in API response.");
            }
            if (data.pendingLabRequests !== undefined) {
                document.getElementById('pendingRequest').textContent = data.pendingLabRequests;
            } else {
                console.error("Pending Requests  is missing in API response.");
            }
            if (data.urgentPendingLabRequests !== undefined) {
                document.getElementById("urgentPending").textContent = data.urgentPendingLabRequests;
            } else {
                console.error("Pending Requests  is missing in API response.");
            }
        })
});


