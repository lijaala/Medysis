

document.addEventListener("DOMContentLoaded", function () {



    fetchRoles();
    fetchStaffData();
    loadProfileData();
    setupProfileFormSubmission();
    loadProfilePicture();

})

async function fetchRoles() {
    try {
        const response = await fetch('api/roles');
        console.log(response);
        if (!response.ok) {
            throw new Error("Failed to fetch roles");

        }
        const roles = await response.json();
        populateRolesDropdown(roles);


    } catch (error) {
        console.log('Error fetching roles:', error)
    }
}

function populateRolesDropdown(roles) {
    const roleSelect = document.querySelector('.role');

    roles.forEach(role => {
        console.log(roles);
        const option = document.createElement('option');
        option.value = role.roleID;
        option.textContent = role.role;
        roleSelect.appendChild(option);
    })
}

function openAddStaffForm() {
    const staffForm = document.getElementById("addStaffModal");
    staffForm.style.display = "flex";

}

function closeStaffModal() {
    const staffForm = document.getElementById("addStaffModal");
    staffForm.style.display = "none";

}
function addStaff(event) {
    event.preventDefault();
    let form = document.getElementById("addStaffForm");
    let formData = new FormData(form); // Collect form data

    fetch("/api/admin/addStaff", {
        method: "POST",
        body: formData
    })
        .then(response => response.text()) // Convert response to text
        .then(message => {
            let responseMessage = document.getElementById("responseMessage");
            responseMessage.textContent = message;
            responseMessage.style.color = message.includes("Error") ? "red" : "green"; // Color feedback

            setTimeout(() => {
                responseMessage.textContent = "";
                if (!message.includes("Error")) {
                    closeStaffModal(); // Close modal on success
                    form.reset(); // Clear form fields
                }
            }, 5000);
        })
        .catch(error => {
            let responseMessage = document.getElementById("responseMessage");
            responseMessage.textContent = "Error: " + error;
            responseMessage.style.color = "red";

            setTimeout(() => {
                responseMessage.textContent = "";
            }, 5000);
        });
}


function openEditStaffModal(staffID) {
    fetch(`http://localhost:8081/api/staff/all`)
        .then(response => response.json())
        .then(data => {
            const staff = data.find(s => s.staffID === staffID);
            if (staff) {
                document.getElementById("editStaffID").value = staffID;

                // Ensure correct time format (HH:mm)
                const startTime = staff.startTime ? staff.startTime.substring(0, 5) : "";
                const endTime = staff.endTime ? staff.endTime.substring(0, 5) : "";

                // Set values and trigger reflow
                const startTimeInput = document.getElementById("editStartTime");
                const endTimeInput = document.getElementById("editEndTime");

                startTimeInput.value = startTime;
                endTimeInput.value = endTime;

                // Force visibility update (in case styles hide it)
                startTimeInput.style.display = "inline-block";
                endTimeInput.style.display = "inline-block";

                console.log("Start Time Set:", startTime);
                console.log("End Time Set:", endTime);

                // Show the modal after setting values
                document.getElementById("editStaffModal").style.display = "flex";
            }
        })
        .catch(error => console.error("Error fetching staff data:", error));
}


function closeEditStaffModal() {
    const staffForm = document.getElementById("editStaffModal");
    staffForm.style.display = "none";

}

function fetchStaffData() {
    fetch("http://localhost:8081/api/staff/all") // Replace with your actual API URL
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch staff data");
            }
            return response.json();
        })
        .then(data => {
            populateStaffTable(data);
        })
        .catch(error => {
            console.error("Error fetching staff data:", error);
        });
}

function populateStaffTable(staffList) {
    const tableBody = document.getElementById("staffTableBody");
    tableBody.innerHTML = ""; // Clear existing table data


    staffList.forEach(staff => {
        const row = document.createElement("tr");
        row.setAttribute('data-id', staff.staffID);

        let editButton = "";

        if (staff.role && staff.role.role === "ROLE_DOCTOR") {
            editButton = `
                <button class="edit" onclick="openEditStaffModal('${staff.staffID}')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                            <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                        </g>
                    </svg>
                </button>
            `;
        } else {
            editButton = `<button class="edit"></button>`
        }

        let deleteButton = `
            <button class="delete" onclick="deleteStaff('${staff.staffID}')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M18 19a3 3 0 0 1-3 3H8a3 3 0 0 1-3-3V7H4V4h4.5l1-1h4l1 1H19v3h-1zM6 7v12a2 2 0 0 0 2 2h7a2 2 0 0 0 2-2V7zm12-1V5h-4l-1-1h-3L9 5H5v1zM8 9h1v10H8zm6 0h1v10h-1z" stroke-width="0.5" stroke="currentColor"/></svg>
            </button>
        `;

        let simplifiedRole = "N/A"; // Default role if none found
        if (staff.role.role) {
            switch (staff.role.role) {
                case "ROLE_ADMIN":
                    simplifiedRole = "Admin";
                    break;
                case "ROLE_DOCTOR":
                    simplifiedRole = "Doctor";
                    break;
                case "ROLE_LAB TECHNICIAN":
                    simplifiedRole = "Lab Technician";
                    break;
                default:
                    simplifiedRole = "N/A";
            }
        }

        row.innerHTML = `
            <td>${staff.staffName}</td>
            <td>${simplifiedRole}</td>
            <td>${staff.staffEmail}</td>
            <td>${staff.staffPhone}</td>
            <td class="actions">
                
                ${deleteButton}
                ${editButton}  

                
            </td>
        `;

        tableBody.appendChild(row);
    });
}



function loadProfileData() {
    fetch("/api/staff/getProfile")
        .then(response => response.json())
        .then(data => {
            document.getElementById("setting-staffName").value = data.staffName || "";
            document.getElementById("setting-staffEmail").value = data.staffEmail || "";
            document.getElementById("setting-staffPhone").value = data.staffPhone || "";
            document.getElementById("setting-staffAddress").value = data.staffAddress || "";
            document.getElementById("setting-gender").value = data.gender || "other";
            document.getElementById("setting-age").value = data.age || "";

            function formatTime(time) {
                return time ? time.substring(0, 5) : ""; // Extract only HH:mm
            }

            if (data.startTime) {
                document.getElementById("setting-startTime").value = formatTime(data.startTime);
            }
            if (data.endTime) {
                document.getElementById("setting-endTime").value = formatTime(data.endTime);
            }
        })
        .catch(error => console.error("Error fetching profile data:", error));

}

function setupProfileFormSubmission() {
    document.getElementById("updateProfileForm").addEventListener("submit", function (event) {
        event.preventDefault();
        console.log("Form submission triggered!");  // Debugging check

        function formatTimeForAPI(time) {
            return time ? time + ":00" : null; // Append ":00" for seconds
        }

        const formData = {
            staffName: document.getElementById("setting-staffName").value,
            staffEmail: document.getElementById("setting-staffEmail").value,
            staffPhone: document.getElementById("setting-staffPhone").value,
            staffAddress: document.getElementById("setting-staffAddress").value,
            gender: document.getElementById("setting-gender").value,
            age: document.getElementById("setting-age").value,
            startTime: formatTimeForAPI(document.getElementById("setting-startTime")?.value),
            endTime: formatTimeForAPI(document.getElementById("setting-endTime")?.value)
        };

        fetch("/api/staff/update", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data =>
                Toastify({
                text: "Profile Updated successfully",
                duration: 3000,

                newWindow: true,

                gravity: "top", // `top` or `bottom`
                position: "right", // `left`, `center` or `right`
                stopOnFocus: true, // Prevents dismissing of toast on hover
                style: {
                    backgroundColor: "#F0F2F9FF",
                    borderRadius:"12px"
                },
                onClick: function(){} // Callback after click
            }).showToast()
            )
            .catch(error => alert("Error updating profile.")
            );
    });
}
function loadProfilePicture() {
    fetch("api/staff/current")
        .then(response => response.json())
        .then(data => {
            const profileImg = document.getElementById("profileImage");
            if (data.profilePicture) {
                profileImg.src = data.profilePicture;
            } else {
                profileImg.src = "/default-profile.png"; // Fallback image
            }
        })
        .catch(error => console.error("Error fetching profile picture:", error));
}


function resetPassword(event) {
    event.preventDefault();

    let currentPassword = document.getElementById("currentPassword").value;
    let newPassword = document.getElementById("newPassword").value;

    fetch("api/staff/reset-password", {
        method: "POST",
        body: new URLSearchParams({ currentPassword, newPassword }),
        headers: { "Content-Type": "application/x-www-form-urlencoded" }
    })
        .then(response => response.json())
        .then(data => {

            Toastify({
                text: data.message,
                duration: 1500,
                backgroundColor: "rgba(200,253,223,0.5)",
                gravity: "top",
                position: "right",

                style:{

                    color:"rgb(15,94,27)",
                    borderRadius:"8px"
                },onClick: function(){}
            }).showToast();

            if (data.redirectUrl) {

                window.location.href = data.redirectUrl; // ✅ Redirect to login
            }
        })
        .catch(error => {
            console.error("Error:", error)
            Toastify({
                text: "Error",
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

        });
}



// Function to handle profile picture upload
function selectProfilePicture() {
    document.getElementById("profileImageInput").click();
}

function updateProfilePicture() {
    const fileInput = document.getElementById("profileImageInput");
    if (fileInput.files.length > 0) {
        let formData = new FormData();
        formData.append("photo", fileInput.files[0]); // Corrected line

        fetch("api/staff/update-profile-picture", {
            method: "POST",
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                console.log("Server Response:", data);
                alert(data.message);
                Toastify({
                    text: data.message,
                    duration: 1500,
                    backgroundColor: "rgba(200,253,223,0.5)",
                    gravity: "top",
                    position: "right",

                    style:{

                        color:"rgb(15,94,27)",
                        borderRadius:"8px"
                    },onClick: function(){}
                }).showToast();

                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                } else {
                    loadProfilePicture();
                }
            })
            .catch(error => console.error("Error:", error));
    } else {

        Toastify({
            text: "Please select an image first.",
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
    }
}
// Function to delete profile picture (if needed)
function deleteProfilePicture() {
    fetch("/api/staff/delete-profile-picture", { method: "DELETE" })
        .then(response => response.text())
        .then(() => {
            document.getElementById("profileImage").src = "/default-profile.png";

            Toastify({
                text: "Profile picture deleted!",
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

        })
        .catch(error => alert("Error deleting profile picture."));
}



