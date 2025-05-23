let originalPatientList = [];

document.addEventListener("DOMContentLoaded", function (){
        fetchPatientData();
        document.querySelectorAll('.Table thead td[data-sort]').forEach(header => {
            header.addEventListener('click', () => {
                const sortBy = header.getAttribute('data-sort');
                const order = header.getAttribute('data-order');
                const newOrder = order === 'asc' ? 'desc' : 'asc';
                header.setAttribute('data-order', newOrder);

                // Remove active class from other headers
                document.querySelectorAll('.Table thead td').forEach(h => {
                    h.classList.remove('sort-active', 'sort-asc', 'sort-desc');
                });

                // Add active class to the clicked header
                header.classList.add('sort-active', newOrder === 'asc' ? 'sort-asc' : 'sort-desc');

                applyPatientSort(sortBy, newOrder); // Call applyPatientSort with sort parameters
            });
        });

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
                originalPatientList = data;
                populatePatientTable(data);
            })
            .catch(error=>{
                console.error("Error fetching patient Data", error);
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
                    
                </td>
            `;

            tableBody.appendChild(row);
        });
    }
    function editPatientRow(userID) {
        fetch(`/api/user/${userID}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch user details");
                }
                return response.json();
            })
            .then(patient => {
                document.getElementById("nameEdit").value = patient.name || "";
                document.getElementById("emailEdit").value = patient.email || "";
                document.getElementById("phoneEdit").value = patient.phone || "";
                document.getElementById("addressEdit").value = patient.address || "";
                document.getElementById("ageEdit").value = patient.age || "";
                document.getElementById("genderEdit").value = patient.gender || "Other";


                document.getElementById("editUserButton").setAttribute("data-user-id", userID);
                // Show the modal
                document.getElementById("editPatientModal").style.display = "flex";
            })
            .catch(error => {
                console.error("Error fetching patient details:", error);
            });
    }
    // Function to close the edit modal
    function closeEditPatient() {
        document.getElementById("editPatientModal").style.display = "none";
    }
    document.getElementById("editUserButton").addEventListener("click", function () {
        const userID = this.getAttribute("data-user-id");
        savePatientRow(userID);
    });




    function savePatientRow(userID) {
        const formData = new FormData();
        formData.append("name", document.getElementById("nameEdit").value.trim());
        formData.append("email", document.getElementById("emailEdit").value.trim());
        formData.append("phone", document.getElementById("phoneEdit").value.trim());
        formData.append("address", document.getElementById("addressEdit").value.trim());
        formData.append("age", document.getElementById("ageEdit").value ? document.getElementById("ageEdit").value.trim() : "");
        formData.append("gender", document.getElementById("genderEdit").value.trim());

        console.log("Sending FormData:", Object.fromEntries(formData.entries()));

        fetch(`/api/user/update/${userID}`, {
            method: "PUT",
            body: formData,
            credentials: "include" // Important if using session authentication
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

                console.log("Update Response:", data);
                fetchPatientData(); // Refresh the table
                closeEditPatient(); // Close the modal
            })
            .catch(error => {
                Toastify({
                    text: "Error updating patient:",
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

                console.error("Error updating patient:", error);
            });
    }

    function openAddPatient(){
        const patientModal=document.getElementById("addPatientModal");
        patientModal.style.display="flex";
    }
    function closeAddPatient(){
        const patientModal=document.getElementById("addPatientModal");
        patientModal.style.display="none";

    }
    function applyPatientSort(sortBy, order) {
        fetch("api/user/all")
            .then(response => response.json())
            .then(patients => {
                patients.sort((a, b) => {
                    let valueA, valueB;

                    switch (sortBy) {
                        case 'name':
                            valueA = a.name || '';
                            valueB = b.name || '';
                            break;
                        case 'email':
                            valueA = a.email || '';
                            valueB = b.email || '';
                            break;
                        case 'phone':
                            valueA = a.phone || '';
                            valueB = b.phone || '';
                            break;
                        case 'verified':
                            valueA = a.verified ? 'TRUE' : 'FALSE';
                            valueB = b.verified ? 'TRUE' : 'FALSE';
                            break;
                        default:
                            return 0;
                    }

                    const comparison = valueA.localeCompare(valueB);
                    return order === 'asc' ? comparison : -comparison;
                });
                populatePatientTable(patients);
            })
            .catch(error => console.error('Error fetching patients:', error));
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

        fetch('/api/auth/signup', {
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
function filterPatients(query) {
    console.log("filterPatients called with query:", query);
    console.log("originalPatientList:", originalPatientList);
    const filteredPatients = originalPatientList.filter(patient =>
        patient.name.toLowerCase().includes(query.toLowerCase())
    );
    populatePatientTable(filteredPatients);
}