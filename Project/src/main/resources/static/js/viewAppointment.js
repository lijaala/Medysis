/*fuction for dynamic table content*/

document.addEventListener("DOMContentLoaded", async () => {
    try {
        const roleResponse = await fetch('api/auth/role', { method: 'GET', credentials: 'same-origin' });
        if (!roleResponse.ok) {
            console.error("Failed to fetch role:", roleResponse.status);
            return;
        }
        const userRole = await roleResponse.text();
        console.log("User Role:", userRole);

        const appointmentResponse = await fetch('/appointment/list', { method: 'GET' });
        if (!appointmentResponse.ok) {
            console.error("Failed to fetch appointments:", appointmentResponse.status);
            return;
        }
        const appointments = await appointmentResponse.json();
        const tbody = document.getElementById('appointmentTableBody');
        const doctorColumnIndex = 1;

        if (userRole === 'ROLE_DOCTOR') {
            const headers = document.querySelectorAll('.Table thead tr td');
            headers[doctorColumnIndex].style.display = 'none';
        }

        if (Array.isArray(appointments)) {
            appointments.forEach(appointment => {
                const row = document.createElement('tr');
                row.setAttribute('data-id', appointment.appointmentID);
                row.setAttribute('data-user-id', appointment.patientID.userID);

                row.innerHTML = `
                        <td>${appointment.patientID?.name || 'Unknown Patient'}</td>
                        <td>${appointment.doctorID?.staffName || 'Unknown Doctor'}</td>
                        <td>${appointment.appDate || 'N/A'}</td>
                        <td>${appointment.appTime || 'N/A'}</td>
                        <td>${appointment.status || 'N/A'}</td>
                        <td class="actions">
                            <button type="button" class="edit" onclick="openEditModal('${appointment.appointmentID}', '${appointment.appDate}', '${appointment.appTime}', '${appointment.status}')">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
\t<g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
\t\t<path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
\t\t<path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
\t</g>
</svg></button>
                            ${userRole === 'ROLE_DOCTOR' ? `<button type="submit" class="primary" onclick="startAppointment('${appointment.appointmentID}')">Start</button>` : ''}
                        </td>
                    `;
                tbody.appendChild(row);
            });

            if (userRole === 'ROLE_DOCTOR') {
                const rows = tbody.querySelectorAll('tr');
                rows.forEach(row => {
                    const cells = row.querySelectorAll('td');
                    if (cells.length > doctorColumnIndex) {
                        cells[doctorColumnIndex].style.display = 'none';
                    }
                });
            }
        } else {
            tbody.innerHTML = `<tr><td colspan="6">No appointments found</td></tr>`;
        }
    } catch (error) {
        console.error("Error fetching data:", error);
    }
});
/*function for opening appointmnet editing modal*/
function openEditModal(id, appDate, appTime, status) {


    document.getElementById('editAppointmentID').value = id;
    document.getElementById('editAppDate').value = appDate;
    document.getElementById('editAppTime').value = appTime;
    document.getElementById('editStatus').value = status;
    document.getElementById('editModal').style.display = 'flex';
}
//function to close editing modal
function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}
//function to save appointment changes
function saveAppointmentChanges() {
    const appointmentID = document.getElementById("editAppointmentID").value;
    const appDate = document.getElementById("editAppDate").value;
    const appTime = document.getElementById("editAppTime").value;
    const status = document.getElementById("editStatus").value;
    const modalContent= document.querySelector(".modal-content");

    // Prepare the form data
    const data = new URLSearchParams();
    data.append("appointmentID", appointmentID);
    data.append("date", appDate || '');  // Send empty string if no date is selected
    data.append("time", appTime || '');  // Send empty string if no time is selected
    data.append("status", status || '');  // Send empty string if no status is selected

    fetch("/appointment/edit", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: data.toString(),
    })
        .then(response => response.text())
        .then(data => {
            console.log(data); // Show success message
// Update the table row dynamically
            const row = document.querySelector(`tr[data-id='${appointmentID}']`);
            if (row) {
                row.children[2].textContent = appDate || 'N/A';  // Update Date column
                row.children[3].textContent = appTime || 'N/A';  // Update Time column
                row.children[4].textContent = status || 'N/A';   // Update Status column
            }
//update status modal
            modalContent.innerHTML = `

            <h3>${status === "Cancelled" ? "Appointment Cancelled" : "Appointment Updated"}</h3>
            <p>${status === "Cancelled" ? "This appointment has been successfully cancelled." : "The appointment details have been updated successfully."}</p>
        `;

            // Hide modal after 3 seconds
            setTimeout(() => {
                closeEditModal();

            }, 5000);
        })
        .catch(error => {
            console.error("Error editing appointment:", error);
        });
}
