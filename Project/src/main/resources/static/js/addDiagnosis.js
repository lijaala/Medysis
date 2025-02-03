function startAppointment(appointmentID) {
    console.log("Start Appointment triggered for ID:", appointmentID);

    // Fetch the appointment row based on the appointment ID
    const appointmentRow = document.querySelector(`tr[data-id='${appointmentID}']`);
    if (!appointmentRow) {
        console.error(`Appointment row not found for ID: ${appointmentID}`);
        return;
    }

    // Get user ID from the row's dataset
    const userID = appointmentRow.dataset.userId;
    if (!userID) {
        console.error("User ID not found");
        return;
    }

    // Ensure elements exist before setting values
    const userIdInput = document.getElementById("userIdInput");
    const appointmentIdInput = document.getElementById("appointmentIdInput");
    if (!userIdInput || !appointmentIdInput) {
        console.error("Hidden input fields not found in the modal!");
        return;
    }

    // Assign values
    userIdInput.value = userID;
    appointmentIdInput.value = appointmentID;
    console.log("UserID:", userID)

    // Open the diagnosis modal
    openDiagnosisModal();
}
document.addEventListener('DOMContentLoaded', function() {
    // Attach event listeners to buttons after the page is loaded
    const nextButton = document.getElementById('nextButton');
    const doneButton = document.getElementById('doneButton');






    // Next button handler
    nextButton.addEventListener('click', function(event) {
        saveDiagnosisAndNext(event);
    });

    // Done button handler
    doneButton.addEventListener('click', function(event) {
        saveDiagnosisAndClose(event);
    });
});
function saveDiagnosis(event, callback) {

    event.preventDefault();

    const form = document.getElementById("diagnosisForm");
    const formData = new FormData(form);

    fetch("/api/medicalRecords/saveDiagnosis", {
        method: "POST",
        body: formData
    })
        .then(response => response.text())
        .then(message => {
            alert("Diagnosis saved successfully!");
            if (callback) callback();
        })
        .catch(error => console.error("Error saving diagnosis:", error));
}

function saveDiagnosisAndNext(event) {
    saveDiagnosis(event,() => {
        closeDiagnosisModal();
        openPrescriptionModal(
            document.getElementById("appointmentIdInput").value,
            document.getElementById("userIdInput").value,
            document.getElementById("doctorIdInput").value
        );
    });
}

function saveDiagnosisAndClose(event) {
    saveDiagnosis(event,() => {
        closeDiagnosisModal();
    });
}

function openDiagnosisModal() {
    const diagnosisModal = document.getElementById("diagnosisModal");

    if (!diagnosisModal) {
        console.error("Diagnosis modal not found!");
        return;
    }

    // Simply open the modal
    diagnosisModal.style.display = "flex";
}



function closeDiagnosisModal() {
    const diagnosisModal = document.getElementById("diagnosisModal");
    diagnosisModal.style.display = "none";
}


function viewPastRecords() {
    const userID = document.getElementById("userIdInput").value;

    fetch(`/api/medicalRecords/getByUserId?userID=${userID}`)
    .then(response => response.json())
    .then(data => {
    const tableBody = document.getElementById("medicalRecordsBody");
    tableBody.innerHTML = ""; // Clear previous data

    data.forEach(record => {
    const row = document.createElement("tr");

    // Condition Name
    const conditionCell = document.createElement("td");
    conditionCell.textContent = record.conditionName;
    row.appendChild(conditionCell);

    // Diagnosed Date
    const dateCell = document.createElement("td");
    dateCell.textContent = record.diagnosedDate;
    row.appendChild(dateCell);

    // Status (Editable)
    const statusCell = document.createElement("td");
    const statusSelect = document.createElement("select");
    statusSelect.innerHTML = `
                        <option value="Ongoing" ${record.isTreated === 'Ongoing' ? 'selected' : ''}>Ongoing</option>
                        <option value="Completed" ${record.isTreated === 'Completed' ? 'selected' : ''}>Completed</option>
                    `;
    statusSelect.setAttribute("data-record-id", record.recordID);
    statusCell.appendChild(statusSelect);
    row.appendChild(statusCell);

    tableBody.appendChild(row);
});

    document.getElementById("medicalHistoryModal").style.display = "flex";
})
    .catch(error => console.error("Error fetching medical history:", error));
}




function updateMedicalHistory() {
    const statusUpdates = [];

    document.querySelectorAll("#medicalRecordsBody select").forEach(select => {
    statusUpdates.push({
    recordID: select.getAttribute("data-record-id"),
    isTreated: select.value
});
});

    fetch("/api/medicalRecords/updateStatus", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(statusUpdates)
})
    .then(response => response.text())
    .then(message => {
    alert(message);
    closeMedicalHistoryModal();
})
    .catch(error => console.error("Error updating status:", error));
}


function closeMedicalHistoryModal() {
    document.getElementById("medicalHistoryModal").style.display = "none";
}



function openPrescriptionModal(appointmentId, userId) {
    console.log("Opening prescription modal for Appointment ID:", appointmentId, "User ID:", userId);

    // Ensure that appointmentId and userId are set
    if (!appointmentId || !userId) {
        alert("Appointment ID and User ID are required!");
        return;
    }

    document.getElementById('appointmentId').value = appointmentId;
    document.getElementById('userId').value = userId;
    document.getElementById('prescriptionModal').style.display = 'flex';

    const form = document.getElementById('prescriptionForm');

    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Prevent default form submission

        // Collecting form data manually
        const medications = [];
        const medicationNames = document.querySelectorAll('input[name="medicationName[]"]');
        const dosages = document.querySelectorAll('input[name="dosage[]"]');
        const intakes = document.querySelectorAll('input[name="intake[]"]');
        const frequencies = document.querySelectorAll('input[name="frequency[]"]');//interval
        const daysOfIntakes = document.querySelectorAll('input[name="daysOfIntake[]"]');

        for (let i = 0; i < medicationNames.length; i++) {
            const medication = {
                medicationName: medicationNames[i].value,
                dosage: dosages[i].value,
                intake: intakes[i].value,
                medicationInterval: frequencies[i].value,
                daysOfIntake: parseInt(daysOfIntakes[i].value, 10) // Ensure it's an integer
            };
            medications.push(medication);
        }
        medications.forEach((medication, index) => {
            console.log(`Medication ${index + 1}:`, medication);
        });

        const userID = document.getElementById('userId').value;
        const appointmentId=document.getElementById('appointmentId').value

        const data = {
            user: { userID: parseInt(userID, 10) },
            appointment: { appointmentId: parseInt(appointmentId, 10) },
            prescribedMedications: medications.map(medication => ({
                medication: { medicationName: medication.medicationName }, // Correct: Medication object with name only
                dosage: medication.dosage,                               // Dosage directly under prescribedMedications
                intake: medication.intake,                               // Intake directly under prescribedMedications
                medicationInterval: medication.medicationInterval,         // Interval directly under prescribedMedications
                daysOfIntake: medication.daysOfIntake                     // daysOfIntake directly under prescribedMedications
            }))
        };


        console.log("Submitting prescription data:", data);

        fetch(`/api/prescriptions/add/${appointmentId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(data)
        })
            .then(response => response.json())  // Parse response as JSON
            .then(data => {
                alert("Prescription saved successfully!");
                closePrescriptionModal();
            })
            .catch(error => {
                console.error("Error saving prescription:", error);
                alert("An error occurred while saving the prescription.");
                // Log the response error if available
                if (error.response) {
                    error.response.json().then(errorData => {
                        console.error("Error details from server:", errorData);
                    });
                }
            });


    }, { once: true });  // Ensure the event doesn't bind multiple times
}

// Function to close the Prescription modal
function closePrescriptionModal() {
    document.getElementById('prescriptionModal').style.display = 'none';
}

// Function to add new medication entry
function addMedication() {
    // Get the medications container
    const medicationsContainer = document.getElementById('medicationsContainer');

    // Create a new medication entry div
    const medicationEntry = document.createElement('div');
    medicationEntry.classList.add('medication-entry');

    // Set the HTML for the new medication entry
    medicationEntry.innerHTML = `
        <label for="medication">Select Medication:</label>
        <input type="text" name="medicationName[]" required placeholder="Enter medication name">

        <label for="dosage">Dosage:</label>
        <input type="text" name="dosage[]" required placeholder="e.g., 500mg">

        <label for="intake">Intake:</label>
        <input type="text" name="intake[]" required placeholder="e.g., Oral">

        <label for="frequency">Frequency:</label>
        <input type="text" name="frequency[]" required placeholder="e.g., Every 6 hours">

        <label for="daysOfIntake">Duration (days):</label>
        <input type="number" name="daysOfIntake[]" min="1" required>

        <button type="button" class="remove-medication" onclick="removeMedication(this)">Remove Medication</button>
    `;

    // Append the new medication entry to the container
    medicationsContainer.appendChild(medicationEntry);
}

// Function to remove a medication entry
function removeMedication(button) {
    // Find the parent medication entry
    const medicationEntry = button.closest('.medication-entry');
    // Remove the entry from the container
    medicationEntry.remove();
}


