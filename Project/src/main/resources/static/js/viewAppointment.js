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
        console.log("Appointments data:", appointments); // Inspect data
        const tbody = document.getElementById('appointmentTableBody');
        const doctorColumnIndex = 1;

        tbody.innerHTML = ''; // Clear the table before adding new rows

        if (userRole.includes("DOCTOR")) {
            // Remove doctor column from the table header
            const headerCells = document.querySelectorAll('.appTable thead tr td.doctor-column');
            headerCells.forEach(cell => cell.remove());
        }

        if (Array.isArray(appointments)) {
            appointments.forEach(appointment => {
                const row = document.createElement('tr');
                row.setAttribute('data-id', appointment.appointmentID);
                row.setAttribute('data-user-id', appointment.patientID.userID);

                row.innerHTML = `
                        <td>${appointment.patientID?.name || 'Unknown Patient'}</td>
                        <td class="doctor-column">${appointment.doctorID?.staffName || 'Unknown Doctor'}</td>
                        <td>${appointment.appDate || 'N/A'}</td>
                        <td>${appointment.appTime || 'N/A'}</td>
                        <td>${appointment.status || 'N/A'}</td>
                        <td class="actions">
                            <button type="button" class="edit" onclick="openEditModal('${appointment.appointmentID}', '${appointment.appDate}', '${appointment.appTime}', '${appointment.status}')">
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
const closeAppEdit=document.getElementById('closeAppEdit');
const saveAppEdit=document.getElementById('saveAppEdit');
const closeAddApp=document.getElementById('closeAddApp');

closeAppEdit.addEventListener('click',closeEditModal);
saveAppEdit.addEventListener('click',saveAppointmentChanges);
closeAddApp.addEventListener('click', closeAddAppointment);

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
            console.log(data);

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

            Toastify({
                text:  "Appointment Edited Successfully!",
                duration: 1500,
                backgroundColor: "rgba(200,253,223,0.5)",
                gravity: "top",
                position: "right",

                style:{

                    color:"rgb(15,94,27)",
                    borderRadius:"8px"
                },onClick: function(){}
            }).showToast();

            // Hide modal after 3 seconds
            setTimeout(() => {

                closeEditModal();

            }, 5000);
        })
        .catch(error => {
            console.error("Error editing appointment:", error);
        });
}

function openAddAppointment(){
    const addApp=document.getElementById("addAppModal");
    addApp.style.display="flex";
    populateDropdowns();
}
const openAddApp=document.getElementById('openAddApp');
openAddApp.addEventListener("click", openAddAppointment);

function closeAddAppointment(){
    const addApp=document.getElementById("addAppModal");
    addApp.style.display="none";}


async function populateDropdowns() {
    try {
        // Fetch patients
        const patientResponse = await fetch('/api/user/all', { method: 'GET' });
        if (!patientResponse.ok) {
            console.error("Failed to fetch patients:", patientResponse.status);
            return;
        }
        const patients = await patientResponse.json();
        const patientDropdown = document.getElementById("patientDropdown");
        patientDropdown.innerHTML = '<option value="">Select Patient</option>';

        patients.forEach(patient => {
            const option = document.createElement("option");
            option.value = patient.userID;
            option.textContent = patient.name;
            patientDropdown.appendChild(option);
        });

        // Fetch doctors
        const doctorResponse = await fetch('/appointment/fetchDoctors', { method: 'POST' });
        if (!doctorResponse.ok) {
            console.error("Failed to fetch doctors:", doctorResponse.status);
            return;
        }
        const doctors = await doctorResponse.json();
        const doctorDropdown = document.getElementById("doctorDropdown");
        doctorDropdown.innerHTML = '<option value="">Select Doctor</option>';

        doctors.forEach(doctor => {
            const option = document.createElement("option");
            option.value = doctor.staffID;
            option.textContent = doctor.staffName;
            doctorDropdown.appendChild(option);
        });


    } catch (error) {
        console.error("Error fetching dropdown data:", error);
    }

    async function generateTimeSlots(doctorID) {
        if (!doctorID) return;

        const date = document.getElementById("appDate").value;
        if (!date) return;

        const encodedDoctorID = encodeURIComponent(doctorID); // Properly encode doctorID
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


// Event listeners to trigger slot generation
    document.getElementById("doctorDropdown").addEventListener("change", function () {
        generateTimeSlots(this.value);
    });

    document.getElementById("appDate").addEventListener("change", function () {
        const doctorID = document.getElementById("doctorDropdown").value;
        if (doctorID) {
            generateTimeSlots(doctorID);
        }
    });

}

const addAppointmentForm = document.getElementById("addAppointmentForm");

async function handleAddAppointmentSubmit(event) {
    event.preventDefault();

    addAppointmentForm.removeEventListener("submit", handleAddAppointmentSubmit); // Remove the listener

    const submitButton = addAppointmentForm.querySelector("button[type='submit']");
    submitButton.disabled = true; // Disable the button

    const doctorId = document.getElementById("doctorDropdown").value;
    const patientId = document.getElementById("patientDropdown").value;
    const appointmentDate = document.getElementById("appDate").value;
    const appointmentTime = document.getElementById("appTime").value;


    if (!doctorId ||!patientId ||!appointmentDate ||!appointmentTime) {

        Toastify({
            text: "Please fill in all fields before submitting.",
            duration: 3000,
            backgroundColor: "rgba(253,200,200,0.5)",
            close: true,
            gravity: "top",
            position: "right",
            borderRadius:"8px",
            style:{
                color:"rgb(167,6,14)",
                borderRadius:"8px"
            },onClick: function(){}
        }).showToast();

        submitButton.disabled = false;
        //Re-add the event listener.
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

                style:{

                    color:"rgb(15,94,27)",
                    borderRadius:"8px"
                },onClick: function(){}
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
            borderRadius:"8px",
            style:{
                color:"rgb(167,6,14)",
                borderRadius:"8px"
            },onClick: function(){}
        }).showToast();

    } finally {
        submitButton.disabled = false;
        //Re-add the event listener.

    }
}

addAppointmentForm.addEventListener("submit", handleAddAppointmentSubmit);




