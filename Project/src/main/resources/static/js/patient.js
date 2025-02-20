document.addEventListener("DOMContentLoaded", function (){
    fetchPatientData();
})

function fetchPatientData(){
    fetch("api/user/all")
        .then(response=>{
        if(!response.ok){
            throw new Error("Failed to fetch staff Data");
        }
        return response.json();

    })
        .then(data =>{
            populatePatientTable(data);
        })
        .catch(error=>{
            console.error("Error fetching staff Data", error);
        })
}

function populatePatientTable(patientList) {
    const tableBody = document.getElementById("patientTableBody");
    tableBody.innerHTML = "";

    patientList.forEach(patient => {
        const row = document.createElement("tr");
        row.setAttribute("id", `row-${patient.userID}`);

        row.innerHTML = `
            <td class="name">${patient.name}</td>
            <td>${patient.email}</td>
            <td class="phone">${patient.phone}</td>
            <td class="verified">${patient.verified ? "TRUE" : "FALSE"}</td>
            <td class="actions">
                <button class="edit" onclick="editPatientRow('${patient.userID}')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                    <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                    <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                  </g>
                </svg>
                </button>
                <button class="edit" onclick="savePatientRow('${patient.userID}')" style="display: none;">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 20 20">
                <path fill="currentColor" d="M3 5a2 2 0 0 1 2-2h8.379a2 2 0 0 1 1.414.586l1.621 1.621A2 2 0 0 1 17 6.621V15a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zm2-1a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1v-4.5A1.5 1.5 0 0 1 6.5 10h7a1.5 1.5 0 0 1 1.5 1.5V16a1 1 0 0 0 1-1V6.621a1 1 0 0 0-.293-.707l-1.621-1.621A1 1 0 0 0 13.379 4H13v2.5A1.5 1.5 0 0 1 11.5 8h-4A1.5 1.5 0 0 1 6 6.5V4zm2 0v2.5a.5.5 0 0 0 .5.5h4a.5.5 0 0 0 .5-.5V4zm7 12v-4.5a.5.5 0 0 0-.5-.5h-7a.5.5 0 0 0-.5.5V16z" stroke-width="0.5" stroke="currentColor"/>
            </svg>
                </button>
                <button class="delete" onclick="cancelUserEdit('${patient.userID}')" style="display: none;"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                <path fill="currentColor" d="m12 12.708l-5.246 5.246q-.14.14-.344.15t-.364-.15t-.16-.354t.16-.354L11.292 12L6.046 6.754q-.14-.14-.15-.344t.15-.364t.354-.16t.354.16L12 11.292l5.246-5.246q.14-.14.345-.15q.203-.01.363.15t.16.354t-.16.354L12.708 12l5.246 5.246q.14.14.15.345q.01.203-.15.363t-.354.16t-.354-.16z" stroke-width="0.5" stroke="currentColor"/>
            </svg></button>
            </td>
        `;

        tableBody.appendChild(row);
    });
}
// Function to enable editing of a row
function editPatientRow(userID) {
    const row = document.getElementById(`row-${userID}`);
    if (row) {
        const nameCell = row.querySelector(".name");
        const phoneCell = row.querySelector(".phone");
        const verifiedCell = row.querySelector(".verified");

        // Store original values only if they are not already stored
        if (!nameCell.hasAttribute("data-original")) {
            nameCell.setAttribute("data-original", nameCell.textContent.trim());
        }
        if (!phoneCell.hasAttribute("data-original")) {
            phoneCell.setAttribute("data-original", phoneCell.textContent.trim());
        }
        if (!verifiedCell.hasAttribute("data-original")) {
            verifiedCell.setAttribute("data-original", verifiedCell.textContent.trim());
        }

        const nameValue = nameCell.textContent.trim();
        const phoneValue = phoneCell.textContent.trim();
        const verifiedValue = verifiedCell.textContent.trim().toUpperCase() === "TRUE";

        nameCell.innerHTML = `<input type="text" class="name-input" value="${nameValue}">`;
        phoneCell.innerHTML = `<input type="text" class="phone-input" value="${phoneValue}">`;
        verifiedCell.innerHTML = `
            <select class="verified-input">
                <option value="true" ${verifiedValue ? "selected" : ""}>TRUE</option>
                <option value="false" ${!verifiedValue ? "selected" : ""}>FALSE</option>
            </select>
        `;

        row.querySelector(".actions").innerHTML = `
            <button class="edit" onclick="savePatientRow('${userID}')">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 20 20">
                <path fill="currentColor" d="M3 5a2 2 0 0 1 2-2h8.379a2 2 0 0 1 1.414.586l1.621 1.621A2 2 0 0 1 17 6.621V15a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zm2-1a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1v-4.5A1.5 1.5 0 0 1 6.5 10h7a1.5 1.5 0 0 1 1.5 1.5V16a1 1 0 0 0 1-1V6.621a1 1 0 0 0-.293-.707l-1.621-1.621A1 1 0 0 0 13.379 4H13v2.5A1.5 1.5 0 0 1 11.5 8h-4A1.5 1.5 0 0 1 6 6.5V4zm2 0v2.5a.5.5 0 0 0 .5.5h4a.5.5 0 0 0 .5-.5V4zm7 12v-4.5a.5.5 0 0 0-.5-.5h-7a.5.5 0 0 0-.5.5V16z" stroke-width="0.5" stroke="currentColor"/>
            </svg>
</button>
            <button class="delete" onclick="cancelUserEdit('${userID}')">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                <path fill="currentColor" d="m12 12.708l-5.246 5.246q-.14.14-.344.15t-.364-.15t-.16-.354t.16-.354L11.292 12L6.046 6.754q-.14-.14-.15-.344t.15-.364t.354-.16t.354.16L12 11.292l5.246-5.246q.14-.14.345-.15q.203-.01.363.15t.16.354t-.16.354L12.708 12l5.246 5.246q.14.14.15.345q.01.203-.15.363t-.354.16t-.354-.16z" stroke-width="0.5" stroke="currentColor"/>
            </svg>
</button>
        `;
    }
}


function cancelUserEdit(userID) {
    const row = document.getElementById(`row-${userID}`);
    if (row) {
        const nameCell = row.querySelector(".name");
        const phoneCell = row.querySelector(".phone");
        const verifiedCell = row.querySelector(".verified");

        if (nameCell.hasAttribute("data-original")) {
            nameCell.innerHTML = nameCell.getAttribute("data-original");
        }
        if (phoneCell.hasAttribute("data-original")) {
            phoneCell.innerHTML = phoneCell.getAttribute("data-original");
        }
        if (verifiedCell.hasAttribute("data-original")) {
            verifiedCell.innerHTML = verifiedCell.getAttribute("data-original");
        }

        row.querySelector(".actions").innerHTML = `
            <button class="edit" onclick="editPatientRow('${userID}')">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                    <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                    <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                  </g>
                </svg>
</button>
        `;
    }
}


// Function to save the updated values
function savePatientRow(userID) {
    const row = document.getElementById(`row-${userID}`);
    if (row) {
        // Retrieve updated values from inputs
        const updatedName = row.querySelector(".name-input").value;
        const updatedPhone = row.querySelector(".phone-input").value;
        const updatedVerified = row.querySelector(".verified-input").value === "true";

        // Send updated data to backend
        fetch(`api/user/update/${userID}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: updatedName,
                phone: updatedPhone,
                verified: updatedVerified
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to update patient data");
                }
                return response.text();
            })
            .then(message => {
                console.log("Update Successful:", message);

                // Convert input fields back to plain text
                row.querySelector(".name").innerHTML = updatedName;
                row.querySelector(".phone").innerHTML = updatedPhone;
                row.querySelector(".verified").innerHTML = updatedVerified ? "TRUE" : "FALSE";

                // Restore Edit button
                row.querySelector(".actions").innerHTML = `
                <button class="edit" onclick="editPatientRow('${patient.userID}')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                    <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                    <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                  </g>
                </svg>
                </button>            `;
            })
            .catch(error => {
                console.error("Error updating patient data:", error);
                alert("Failed to update patient data. Please try again.");
            });
    }
}


function openAddPatient(){
    const patientModal=document.getElementById("addPatientModal");
    patientModal.style.display="flex";
}
function closeAddPatient(){
    const patientModal=document.getElementById("addPatientModal");
    patientModal.style.display="none";

}


    // Show the popup and spinner
    const popUp = document.getElementById('popUp');
    const popupModal = document.getElementById('popupModal');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const popupMessage = document.getElementById('popupMessage');
    const responseMessageElement = document.getElementById('responseMessage');

    popUp.style.display = 'block';
    popupModal.style.display = 'block';
    loadingSpinner.style.display = 'block';
    popupMessage.innerText = "Sending Email..."; // Reset popup message
    responseMessageElement.innerText = '';

    const formData = new FormData(event.target);

    fetch('http://localhost:8081/api/auth/signup', {
        method: 'POST',
        body: formData
    })
        .then(response => response.text())
        .then(data => {
            loadingSpinner.style.display = 'none'; // Hide spinner

            if (data.includes("success")) {
                popupMessage.innerText = ""; // Clear the "Sending Email..." message
                responseMessageElement.innerText = "Email sent successfully! Please check your inbox.";
                responseMessageElement.classList.add('success');
            } else {
                popupMessage.innerText = ""; // Clear the "Sending Email..." message
                responseMessageElement.innerText = "Failed to send email. Please try again.";
                responseMessageElement.classList.add('error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            loadingSpinner.style.display = 'none'; // Hide spinner
            popupMessage.innerText = ""; // Clear the "Sending Email..." message
            responseMessageElement.innerText = "An error occurred. Please try again later.";
        });
