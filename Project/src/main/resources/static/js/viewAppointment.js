document.addEventListener("DOMContentLoaded", async () => {
    try {
        const roleResponse = await fetch('api/auth/role', { method: 'GET', credentials: 'same-origin' });
        if (!roleResponse.ok) {
            console.error("Failed to fetch role:", roleResponse.status);
            return;
        }
        userRole = await roleResponse.text();
        console.log("User Role:", userRole);

        await fetchAppointments(userRole);
        await populateDoctorFilter();
        document.getElementById('applyFilters').addEventListener('click', applyFilters);

        // Add click event listeners to table headers for sorting
        document.querySelectorAll('.appTable thead td').forEach(header => {
            header.addEventListener('click', () => {
                const sortBy = header.getAttribute('data-sort');
                const order = header.getAttribute('data-order');
                const newOrder = order === 'asc' ? 'desc' : 'asc';
                header.setAttribute('data-order', newOrder);

                // Remove active class from other headers
                document.querySelectorAll('.appTable thead td').forEach(h => {
                    h.classList.remove('sort-active', 'sort-asc', 'sort-desc');
                });

                // Add active class to the clicked header
                header.classList.add('sort-active', newOrder === 'asc' ? 'sort-asc' : 'sort-desc');

                applyFilters(sortBy, newOrder); // Call applyFilters with sort parameters
            });
        });

    } catch (error) {
        console.error("Error on page load:", error);
    }
});

async function fetchAppointments(userRole) {
    try {
        const appointmentResponse = await fetch('/appointment/list', { method: 'GET' });
        if (!appointmentResponse.ok) {
            console.error("Failed to fetch appointments:", appointmentResponse.status);
            return;
        }
        const appointments = await appointmentResponse.json();
        console.log("Appointments data:", appointments);
        const tbody = document.getElementById('appointmentTableBody');

        tbody.innerHTML = '';

        if (userRole.includes("DOCTOR")) {
            const headerCells = document.querySelectorAll('.appTable thead tr td.doctor-column');
            headerCells.forEach(cell => cell.remove());
        }
        displayAppointments(appointments, userRole);
    } catch (error) {
        console.error("Error fetching data:", error);
    }
}

async function populateDoctorFilter() {
    try {
        const doctorResponse = await fetch('/appointment/fetchDoctors', { method: 'POST' });
        if (!doctorResponse.ok) {
            console.error("Failed to fetch doctors:", doctorResponse.status);
            return;
        }
        const doctors = await doctorResponse.json();
        const doctorFilter = document.getElementById('doctorFilter');

        doctors.forEach(doctor => {
            const option = document.createElement("option");
            option.value = doctor.staffID;
            option.textContent = doctor.staffName;
            doctorFilter.appendChild(option);
        });
    } catch (error) {
        console.error("Error fetching doctors:", error);
    }
}

function displayAppointments(appointments, userRole) {
    const tbody = document.getElementById('appointmentTableBody');
    tbody.innerHTML = '';
    if (Array.isArray(appointments)) {
        appointments.forEach(appointment => {
            const row = document.createElement('tr');
            row.setAttribute('data-id', appointment.appointmentID);
            row.setAttribute('data-user-id', appointment.patientID.userID);
            row.setAttribute('data-doctor-id', appointment.doctorID?.staffID);
            row.innerHTML = `
                <td>${appointment.patientID?.name || 'Unknown Patient'}</td>
                <td class="doctor-column">${appointment.doctorID?.staffName || 'Unknown Doctor'}</td>
                <td>${appointment.appDate || 'N/A'}</td>
                <td>${appointment.appTime || 'N/A'}</td>
                <td>${appointment.status || 'N/A'}</td>
                <td class="actions">
                    <button type="button" class="edit" onclick="openEditModal('${appointment.appointmentID}', '${appointment.appDate}', '${appointment.appTime}', '${appointment.status}', '${appointment.doctorID?.staffID}')">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                            <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                                <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                            </g>
                        </svg>
                    </button>
                    ${userRole === 'ROLE_DOCTOR' ? `
                        <button type="submit" class="primary" onclick="startAppointment('${appointment.appointmentID}')">Start</button>` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });

        if (userRole.includes("DOCTOR")) {
            const bodyCells = document.querySelectorAll('.appTable tbody tr td.doctor-column');
            bodyCells.forEach(cell => cell.style.display = 'none');
        }
    } else {
        tbody.innerHTML = `<tr><td colspan="6">No appointments found</td></tr>`;
    }
}

async function openEditModal(id, appDate, appTime, status, doctorId) {
    document.getElementById('editAppointmentID').value = id;
    document.getElementById('editAppDate').value = appDate;

    const editAppTimeDropdown = document.getElementById('editAppTime');
    editAppTimeDropdown.innerHTML = '<option value="">Select Time Slot</option>';

    try {
        const encodedDoctorId = encodeURIComponent(doctorId);
        const response = await fetch(`/appointment/availableSlots?doctorID=${encodedDoctorId}&date=${appDate}`);
        if (!response.ok) {
            console.error("Failed to fetch available slots for edit:", response.status);
            return;
        }
        const availableSlots = await response.json();
        console.log(availableSlots);

        if (Array.isArray(availableSlots) && availableSlots.length > 0) {
            availableSlots.forEach(slot => {
                const option = document.createElement('option');
                option.value = slot;
                option.textContent = slot;
                option.selected = (slot === appTime);
                editAppTimeDropdown.appendChild(option);
            });
        } else {
            editAppTimeDropdown.innerHTML = '<option value="">No Slots Available</option>';
        }

        document.getElementById('editStatus').value = status;
        document.getElementById('editModal').style.display = 'flex';
    } catch (error) {
        console.error("Error fetching slots for edit:", error);
    }
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

const closeAppEdit = document.getElementById('closeAppEdit');
const saveAppEdit = document.getElementById('saveAppEdit');
const closeAddApp = document.getElementById('closeAddApp');

closeAppEdit.addEventListener('click', closeEditModal);
saveAppEdit.addEventListener('click', saveAppointmentChanges);
closeAddApp.addEventListener('click', closeAddAppointment);

function saveAppointmentChanges() {
    const appointmentID = document.getElementById("editAppointmentID").value;
    const editAppDateInput = document.getElementById("editAppDate");
    const editAppTimeSelect = document.getElementById("editAppTime");
    const editStatusSelect = document.getElementById("editStatus");

    const appDate = editAppDateInput.value;
    const appTime = editAppTimeSelect.value;
    const status = editStatusSelect.value;

    const data = new URLSearchParams();
    data.append("appointmentID", appointmentID);
    data.append("date", appDate || '');
    data.append("time", appTime || '');
    data.append("status", status || '');

    fetch("/appointment/edit", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: data.toString(),
    })
        .then(response => response.text())
        .then(data => {
            console.log(data);

            fetchAppointments(userRole)

            Toastify({
                text: "Appointment Edited Successfully!",
                duration: 1500,
                backgroundColor: "rgba(200,253,223,0.5)",
                gravity: "top",
                position: "right",
                style: {
                    color: "rgb(15,94,27)",
                    borderRadius: "8px"
                }, onClick: function () {
                }
            }).showToast();

            setTimeout(() => {
                closeEditModal();
                fetchAppointments(userRole);
            }, 3000);
        })
        .catch(error => {
            console.error("Error editing appointment:", error);
        });
}

function openAddAppointment() {
    const addApp = document.getElementById("addAppModal");
    addApp.style.display = "flex";
    populateDropdownsForAdd();
}

const openAddApp = document.getElementById('openAddApp');
openAddApp.addEventListener("click", openAddAppointment);

function closeAddAppointment() {
    const addApp = document.getElementById("addAppModal");
    addApp.style.display = "none";
    resetAddAppointmentForm();
}

function resetAddAppointmentForm() {
    const addAppointmentForm = document.getElementById("addAppointmentForm");
    addAppointmentForm.reset();
    const timeDropdown = document.getElementById("appTime");
    timeDropdown.innerHTML = '<option value="">Select Time Slot</option>';
}

async function populateDropdownsForAdd() {
    try {
        const patientResponse = await fetch('/api/user/all', { method: 'GET' });
        if (!patientResponse.ok) {
            console.error("Failed to fetch patients:", patientResponse.status);
            return;
        }
        const patients = await patientResponse.json();
        const patientDropdown = document.getElementById("patientDropdown");
        patientDropdown.innerHTML = '<option value=""> Select Patient</option>';

        patients.forEach(patient => {
            const option = document.createElement("option");
            option.value = patient.userID;
            option.textContent = patient.name;
            patientDropdown.appendChild(option);
        });

        const doctorResponse = await fetch('/appointment/fetchDoctors', { method: 'POST' });
        if (!doctorResponse.ok) {
            console.error("Failed to fetch doctors:", doctorResponse.status);
            return;
        }
        const doctors = await doctorResponse.json();
        const doctorDropdown = document.getElementById("doctorDropdown");
        doctorDropdown.innerHTML = '<option value=""> Select Doctor</option>';

        doctors.forEach(doctor => {
            const option = document.createElement("option");
            option.value = doctor.staffID;
            option.textContent = doctor.staffName;
            doctorDropdown.appendChild(option);
        });
    } catch (error) {
        console.error("Error fetching dropdown data:", error);
    }
}

async function generateTimeSlotsForAdd(doctorID) {
    if (!doctorID) return;

    const date = document.getElementById("appDate").value;
    if (!date) return;

    const encodedDoctorID = encodeURIComponent(doctorID);
    console.log("Fetching slots for:", encodedDoctorID, "on", date);

    try {
        const response = await fetch(`/appointment/availableSlots?doctorID=${encodedDoctorID}&date=${date}`);

        if (!response.ok) {
            console.error("Failed to fetch available slots:", response.status);
            return;
        }

        const availableSlots = await response.json();
        console.log("Slots received:", availableSlots);

        const timeDropdown = document.getElementById("appTime");
        timeDropdown.innerHTML = '<option value="">Select Time Slot</option>';

        if (Array.isArray(availableSlots) && availableSlots.length > 0) {
            availableSlots.forEach(slot => {
                const option = document.createElement("option");
                option.value = slot;
                option.textContent = slot;
                timeDropdown.appendChild(option);
            });
        } else {
            timeDropdown.innerHTML = '<option value="">No Slots Available</option>';
        }
    } catch (error) {
        console.error("Error fetching slots:", error);
    }
}

document.getElementById("doctorDropdown").addEventListener("change", function () {
    generateTimeSlotsForAdd(this.value);
});

document.getElementById("appDate").addEventListener("change", function () {
    const doctorID = document.getElementById("doctorDropdown").value;
    if (doctorID) {
        generateTimeSlotsForAdd(doctorID);
    }
});

const addAppointmentForm = document.getElementById("addAppointmentForm");

async function handleAddAppointmentSubmit(event) {
    event.preventDefault();

    addAppointmentForm.removeEventListener("submit", handleAddAppointmentSubmit);

    const submitButton = addAppointmentForm.querySelector("button[type='submit']");
    submitButton.disabled = true;

    const doctorId = document.getElementById("doctorDropdown").value;
    const patientId = document.getElementById("patientDropdown").value;
    const appointmentDate = document.getElementById("appDate").value;
    const appointmentTime = document.getElementById("appTime").value;

    if (!doctorId || !patientId || !appointmentDate || !appointmentTime) {
        Toastify({
            text: "Please fill in all fields before submitting.",
            duration: 3000,
            backgroundColor: "rgba(253,200,200,0.5)",
            close: true,
            gravity: "top",
            position: "right",
            borderRadius: "8px",
            style: {
                color: "rgb(167,6,14)",
                borderRadius: "8px"
            }, onClick: function () {
            }
        }).showToast();

        submitButton.disabled = false;
        addAppointmentForm.addEventListener("submit", handleAddAppointmentSubmit);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("patientID", patientId);
    formData.append("doctor", doctorId);
    formData.append("date", appointmentDate);
    formData.append("time", appointmentTime);

    try {
        const response = await fetch('/appointment/admin/book', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            credentials: 'same-origin',
            body: formData.toString()
        });

        if (!response.ok) {
            throw new Error("Failed to book appointment.");
        } else {
            Toastify({
                text: "Appointment booked successfully!",
                duration: 1500,
                backgroundColor: "rgba(200,253,223,0.5)",
                gravity: "top",
                position: "right",
                style: {
                    color: "rgb(15,94,27)",
                    borderRadius: "8px"
                }, onClick: function () {
                }
            }).showToast();

            setTimeout(() => {
                closeAddAppointment();
                location.reload();
            }, 3000);
        }
    } catch (error) {
        console.error("Error booking appointment:", error);

        Toastify({
            text: "An error occurred while booking the appointment. Please try again.",
            duration: 3000,
            backgroundColor: "rgba(253,200,200,0.5)",
            close: true,
            gravity: "top",
            position: "right",
            borderRadius: "8px",
            style: {
                color: "rgb(167,6,14)",
                borderRadius: "8px"
            }, onClick: function () {
            }
        }).showToast();
    } finally {
        submitButton.disabled = false;
        addAppointmentForm.addEventListener("submit", handleAddAppointmentSubmit);
    }
}

addAppointmentForm.addEventListener("submit", handleAddAppointmentSubmit);

function applyFilters(sortBy, order) {
    const status = document.getElementById('statusFilter').value;
    const doctorId = document.getElementById('doctorFilter').value;

    fetch('/appointment/list')
        .then(response => response.json())
        .then(appointments => {
            let filteredAppointments = appointments;

            if (status) {
                filteredAppointments = filteredAppointments.filter(appointment => appointment.status === status);
            }

            if (doctorId) {
                filteredAppointments = filteredAppointments.filter(appointment => appointment.doctorID?.staffID === doctorId);
            }

            if (sortBy) { // Only sort if sortBy is provided
                filteredAppointments.sort((a, b) => {
                    let valueA, valueB;

                    switch (sortBy) {
                        case 'patient':
                            valueA = a.patientID?.name || '';
                            valueB = b.patientID?.name || '';
                            break;
                        case 'doctor':
                            valueA = a.doctorID?.staffName || '';
                            valueB = b.doctorID?.staffName || '';
                            break;
                        case 'date':
                            valueA = a.appDate || '';
                            valueB = b.appDate || '';
                            break;
                        case 'time':
                            valueA = a.appTime || '';
                            valueB = b.appTime || '';
                            break;
                        case 'status':
                            valueA = a.status || '';
                            valueB = b.status || '';
                            break;
                        default:
                            return 0;
                    }

                    const comparison = valueA.localeCompare(valueB);
                    return order === 'asc' ? comparison : -comparison;
                });
            }

            displayAppointments(filteredAppointments, userRole);
        })
        .catch(error => console.error('Error fetching appointments:', error));
}