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
    const savePrescriptionButton = document.getElementById('savePrescriptionButton');
    savePrescriptionButton.addEventListener('click', function (event) {
        savePrescription(event);


        // Save button handler


    // Next button handler
    nextButton.addEventListener('click', function(event) {
        saveDiagnosisAndNext(event);
    });

    // Done button handler
    doneButton.addEventListener('click', function(event) {
        saveDiagnosisAndClose(event);
    });
});
})
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



// Function to open the Prescription modal
    function openPrescriptionModal(appointmentId, userId, staffId) {
        document.getElementById('appointmentId').value = appointmentId;
        document.getElementById('userId').value = userId;
        document.getElementById('staffId').value = staffId;
        document.getElementById('prescriptionModal').style.display = 'flex';
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
        <select name="medicationID[]" class="medication" required>
            <option value="">Select Medication</option>
        </select>

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

        // Optionally, fetch the medication options (you can skip this if already done)
        fetchMedications();
    }

// Function to remove a medication entry
    function removeMedication(button) {
        // Find the parent medication entry
        const medicationEntry = button.closest('.medication-entry');
        // Remove the entry from the container
        medicationEntry.remove();
    }

// Fetch medications and populate the select dropdown
    function fetchMedications() {
        fetch('/api/medications')
            .then(response => response.json())
            .then(data => {
                let medicationSelects = document.querySelectorAll('.medication');
                medicationSelects.forEach(select => {
                    data.forEach(medication => {
                        let option = document.createElement('option');
                        option.value = medication.medicationID;
                        option.textContent = medication.medicationName;
                        select.appendChild(option);
                    });
                });
            })
            .catch(error => console.error('Error fetching medications:', error));
    }

// Call this function to load medications on page load
    document.addEventListener('DOMContentLoaded', fetchMedications);
