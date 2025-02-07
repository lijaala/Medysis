//Start appointment flow

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

//event listener for buttons
document.addEventListener('DOMContentLoaded', function() {
    // Attach event listeners to buttons after the page is loaded
    const nextButton = document.getElementById('nextButton');
    const doneButton = document.getElementById('doneButton');

    const prescriptionNextButton = document.getElementById('prescriptionNext');
    const prescriptionDoneButton = document.getElementById('prescriptionDone');


    // Next button handler
    nextButton.addEventListener('click', function(event) {
        saveDiagnosisAndNext(event);
    });

    // Done button handler
    doneButton.addEventListener('click', function(event) {
        saveDiagnosisAndClose(event);
    });

    prescriptionNextButton.addEventListener('click', function (event){
        setTimeout(()=>{
            closePrescriptionModal();
            openLabReportsModal(
                document.getElementById("appointmentId").value,
                document.getElementById("userId").value
            );

        }, 5000)
    });

    prescriptionDoneButton.addEventListener("click", function (event){
        const form= document.getElementById('prescriptionForm');
        form.dispatchEvent(new Event ('submit'));
         setTimeout(closePrescriptionModal, 3000);

    })
});

// open save diagnosis
function saveDiagnosis(event, callback) {

    event.preventDefault();

    const form = document.getElementById("diagnosisForm");
    const formData = new FormData(form);
    const messageDiv = document.getElementById("diagnosis-message"); // Get the message div


    fetch("/api/medicalRecords/saveDiagnosis", {
        method: "POST",
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(err => {throw new Error(err)}); // Get error as text
            }
            return response.text(); // Get success as text
        })
        .then(data => {

            try {
                const jsonData = JSON.parse(data); //try to parse as JSON
                console.error("Server Error:", jsonData.message);
                messageDiv.textContent = "Error: " + jsonData.message;
                messageDiv.className = "message";

            } catch (parseError) {
                console.log("Success:", data);
                messageDiv.textContent = data;
                messageDiv.className = "message";
                if (callback) {
                callback(data);
                } // Pass the success message to the callback
            }


        })
        .catch(error => {
            console.error("Fetch Error:", error);
            messageDiv.textContent = "An error occurred: " + error.message; // Display error message
            messageDiv.className = "message";
        });
}

//open prescription modal and save diagnosis
function saveDiagnosisAndNext(event) {
    saveDiagnosis(event, (message) => {
        if (message === "Success") {
            // Delay closing the diagnosis modal to show the message:
            setTimeout(() => {
                closeDiagnosisModal();
                setTimeout(() => { // Delay opening prescription modal
                    openPrescriptionModal(
                        document.getElementById("appointmentIdInput").value,
                        document.getElementById("userIdInput").value,
                    );
                }, 3000); // Small delay for opening
            }, 3500); // Delay for closing (1.5 seconds)
        }
    });
}
// save diagnosis and close
function saveDiagnosisAndClose(event) {
    saveDiagnosis(event, (message) => {
        if (message === "Success") {
            setTimeout(closeDiagnosisModal, 3000); // Delay for closing
        }
    });
}

//open diagnosis modal
function openDiagnosisModal() {
    const diagnosisModal = document.getElementById("diagnosisModal");

    if (!diagnosisModal) {
        console.error("Diagnosis modal not found!");
        return;
    }

    // Simply open the modal
    diagnosisModal.style.display = "flex";
}


// close diagnosis modal
function closeDiagnosisModal() {
    const diagnosisModal = document.getElementById("diagnosisModal");
    diagnosisModal.style.display = "none";
}

// view past medical records
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


// update the status od medical condition

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

// close the medical history
function closeMedicalHistoryModal() {
    document.getElementById("medicalHistoryModal").style.display = "none";
}



// open prescription modal
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
        const alternative = document.querySelectorAll('input[name="alternative[]"]');//interval

        const daysOfIntakes = document.querySelectorAll('input[name="daysOfIntake[]"]');

        for (let i = 0; i < medicationNames.length; i++) {
            const medication = {
                medicationName: medicationNames[i].value,
                dosage: dosages[i].value,
                intake: intakes[i].value,
                medicationInterval: frequencies[i].value,
                interval:alternative[i].value,
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
                alternative:medication.alternative,
                daysOfIntake: medication.daysOfIntake                     // daysOfIntake directly under prescribedMedications
            }))
        };


        console.log("Submitting prescription data:", data);
        const messageDiv = document.getElementById('prescription-message'); // Get the message div

        fetch(`/api/prescriptions/add/${appointmentId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) { // Check for HTTP errors (status outside 200-299)
                    return response.json().then(err => {throw new Error(err.message)}); // Throw error for catch block
                }
                return response.text();
            })
            .then(data => {
                try {
                    const jsonData = JSON.parse(data); // Try to parse as JSON (error case)
                    console.error("Server Error:", jsonData.message);
                    messageDiv.textContent = "Error: " + jsonData.message;
                    messageDiv.classList.add('message'); // Add a CSS class for styling (optional)


                } catch (parseError) { // It's the success message (plain text)
                    console.log("Success:", data);
                    messageDiv.textContent = data; // Or a more user-friendly message
                    messageDiv.classList.add('message'); // Add CSS class for styling

                    // Optionally, close the modal after a short delay:
                    setTimeout(closePrescriptionModal, 5000);
                }
            })
            .catch(error => {
                console.error("Error saving prescription:", error);
                messageDiv.textContent = "An error occurred: " + error.message;
                messageDiv.classList.add('message');
            });



    }, { once: true });  // Ensure the event doesn't bind multiple times
}

// Function to close the Prescription modal
function closePrescriptionModal() {
    document.getElementById('prescriptionModal').style.display = 'none';
}

// Function to add more than one medication
function addMedication() {
    // Get the medications container
    const medicationsContainer = document.getElementById('medicationsContainer');

    // Create a new medication entry div
    const medicationEntry = document.createElement('div');
    medicationEntry.classList.add('medication-entry');

    // Set the HTML for the new medication entry
    medicationEntry.innerHTML = `
        <div class="form-row">

                            <div class="form-row">
                                <label for="medication">Select Medication:</label>
                                <input type="text" id="medication" name="medicationName[]" required placeholder="Enter medication name">
                            </div>

                        <!-- Dosage Input -->
                            <div class="form-row">
                                <label for="dosage">Dosage:</label>
                                <input type="text" name="dosage[]" id="dosage" required placeholder="e.g., 500mg">
                            </div>
                        </div>  
                        <div class="form-row"> 
                            <div class="form-row">
                                <!-- Intake Instruction -->
                                <label for="intake">Intake:</label>
                                <input type="text" name="intake[]" id="intake" required placeholder="e.g., Oral">
                            </div>
                            <div class="form-row">
                                <!-- Frequency Input -->
                                <label for="frequency">Frequency:</label>
                                <input type="text" name="frequency[]" id="frequency" required placeholder="e.g., Every 6 hours">
                            </div>    
                        </div>
                        <div class="form-row">
                            <!-- Duration Input -->
                           <label for="alternative">Alternative:</label>
                            <input type="text" name="alternative[]" id="alternative" min="1" required>
                        </div>
                        <div class="form-row">       
                             <label for="daysOfIntake">Duration (days):</label>
                             <input type="number" name="daysOfIntake[]" id="daysOfIntake" min="1" required>
                        </div>     


                        <!-- Remove Medication Button -->
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
function openLabReportsModal(appointmentId, userId, doctorId){
    document.getElementById('orderLabReports').style.display = 'flex';

}
function closeLabReportsModal(){
    document.getElementById('orderLabReports').style.display='flex';

}


