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
    //openLabReportsModal(userIdInput,appointmentIdInput);
}



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
                    const appointmentId = document.getElementById("appointmentIdInput").value;
                    const userId = document.getElementById("userIdInput").value;

                    openPrescriptionModal(appointmentId,userId);
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

    const nextButton = document.getElementById('nextButton');
    const doneButton = document.getElementById('doneButton');

    if (!diagnosisModal) {
        console.error("Diagnosis modal not found!");
        return;
    }

    // Simply open the modal
    diagnosisModal.style.display = "flex";

    nextButton.addEventListener('click', function(event) {
        saveDiagnosisAndNext(event);
    });

    // Done button handler
    doneButton.addEventListener('click', function(event) {
        saveDiagnosisAndClose(event, () => { // Callback after diagnosis is saved
            completeAppointment(document.getElementById("appointmentIdInput").value); // Complete appointment
        });
    });
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

    const prescriptionNextButton = document.getElementById('prescriptionNext');
    const prescriptionDoneButton = document.getElementById('prescriptionDone');

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
        setTimeout(() => {
            closePrescriptionModal();
            completeAppointment(document.getElementById("appointmentId").value); // Complete appointment
        }, 3000);

    })

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
        const alternatives = document.querySelectorAll('input[name="alternative[]"]');//interval
        const daysOfIntakes = document.querySelectorAll('input[name="daysOfIntake[]"]');

        for (let i = 0; i < medicationNames.length; i++) {
            const medicationName = medicationNames[i].value.trim(); // Trim whitespace
            const dosage = dosages[i].value.trim();
            const intake = intakes[i].value.trim();
            const frequency = frequencies[i].value.trim();
            const alternative = alternatives[i].value.trim();
            const daysOfIntake = parseInt(daysOfIntakes[i].value, 10);

            if (medicationName === "") { // Frontend validation: Skip empty entries
                console.warn("Skipping empty medication entry.");
                continue; // Go to the next iteration of the loop
            }

            if (isNaN(daysOfIntake) || daysOfIntake <= 0) {
                alert("Days of intake must be a positive number for " + medicationName);
                return; // Stop submission
            }

            const medication = {
                medicationName: medicationName,
                dosage: dosage,
                intake: intake,
                medicationInterval: frequency,
                alternative: alternative,
                daysOfIntake: daysOfIntake
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
let medicationCount = 0;
// Function to add more than one medication
function addMedication() {
    console.log("Adding medication entry. Current count:", medicationCount);  // Log for debugging

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
                                <input type="text" id="medication${medicationCount}" name="medicationName[]" required placeholder="Enter medication name">
                            </div>

                        <!-- Dosage Input -->
                            <div class="form-row">
                                <label for="dosage">Dosage:</label>
                                <input type="text" name="dosage[]" id="dosage${medicationCount}" required placeholder="e.g., 500mg">
                            </div>
                        </div>  
                        <div class="form-row"> 
                            <div class="form-row">
                                <!-- Intake Instruction -->
                                <label for="intake">Intake:</label>
                                <input type="text" name="intake[]" id="intake${medicationCount}" required placeholder="e.g., Oral">
                            </div>
                            <div class="form-row">
                                <!-- Frequency Input -->
                                <label for="frequency">Frequency:</label>
                                <input type="text" name="frequency[]" id="frequency${medicationCount}" required placeholder="e.g., Every 6 hours">
                            </div>    
                        </div>
                        <div class="form-row">
                            <!-- Duration Input -->
                           <label for="alternative">Alternative:</label>
                            <input type="text" name="alternative[]" id="alternative${medicationCount}" min="1" required>
                        </div>
                        <div class="form-row">       
                             <label for="daysOfIntake">Duration (days):</label>
                             <input type="number" name="daysOfIntake[]" id="daysOfIntake${medicationCount}" min="1" required>
                        </div>     


                        <!-- Remove Medication Button -->
                        <button type="button" class="remove-medication" onclick="removeMedication(this)">Remove Medication</button>
                    
    `;

    // Append the new medication entry to the container
    medicationsContainer.appendChild(medicationEntry);
    medicationCount ++;
}

// Function to remove a medication entry
function removeMedication(button) {
    // Find the parent medication entry
    const medicationEntry = button.closest('.medication-entry');
    // Remove the entry from the container
    medicationEntry.remove();
}

function openLabReportsModal(){
    document.getElementById('orderLabReports').style.display = 'flex';

    document.querySelectorAll(".test-dropdown").forEach(select => populateLabTestDropdown(select));

    const existingDropdowns = document.querySelectorAll(".test-dropdown");
    existingDropdowns.forEach(dropdown => populateLabTestDropdown(dropdown));

    const labOrderForm = document.getElementById('labOrder');



    labOrderForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent default form submission
        submitLabOrder(() => {  // Callback after lab order is submitted
            completeAppointment(document.getElementById("appointmentIdInput").value); // Complete appointment
        });
    });


}
function closeLabReportsModal(){
    document.getElementById('orderLabReports').style.display='none';

}

//addLabtest
let testCount = 1; // Start from 1 since the initial entry is already there

async function addLabTest() {
    const container = document.getElementById("labTestsContainer");
    const testEntry = document.createElement("div");
    testEntry.classList.add("lab-test-entry");

    testEntry.innerHTML = `
        <div class="form-row">
            <label for="testName"> Test Name </label>
            <select class="form-select test-dropdown" id="testName${testCount}" name="testName[]">
                <option value="">Select</option>
            </select>
        </div>
        <button type="button" class="remove-medication" onclick="removeLabTest(this)">Remove</button>
    `;

    container.appendChild(testEntry);
    const newDropdown = testEntry.querySelector(".test-dropdown");
    await populateLabTestDropdown(newDropdown);
    console.log(`🆕 New test added. Total dropdowns now: ${document.querySelectorAll('.test-dropdown').length}`);
    testCount++;
}

function removeLabTest(button) {
    button.parentElement.remove();
}

async function populateLabTestDropdown(selectElement) {
    try {
        const response = await fetch("/api/labTests/availableTests"); // Adjust if needed
        if (!response.ok) {
            throw new Error("Failed to fetch lab tests");
        }
        const labTests = await response.json();

        // Populate options
        selectElement.innerHTML = `<option value="">Select</option>`;

        labTests.forEach(test => {
            selectElement.innerHTML += `<option value="${test.testID}">${test.testName} </option>`;
        });
        console.log("Dropdown updated:", selectElement, "Options count:", selectElement.options.length);

    } catch (error) {
        console.error("Error fetching lab tests:", error);
    }
}


function submitLabOrder(callback) {
    console.log(" Submitting lab order...");

    const appointmentId = document.getElementById('appointmentId').value;
    const userId = document.getElementById('userId').value;
    let urgency = document.querySelector('input[name="urgency"]:checked')?.value || "no";

    const testIds = [];
    let allTestsSelected = true; // Assume true initially
    document.querySelectorAll('.lab-test-entry').forEach(entry => {
        const selectElement = entry.querySelector('.test-dropdown');
        if (selectElement && selectElement.value === "") {
            console.warn("🗑 Removing empty dropdown before submission...");
            entry.remove();
        }
    });
    // Loop through all test dropdowns
    document.querySelectorAll('.lab-test-entry').forEach((entry, index) => {
        const selectElement = entry.querySelector('.test-dropdown');
        if (!selectElement) {
            console.error(` Dropdown ${index + 1}: Not found!`);
            allTestsSelected = false;
            return;
        }

        const testId = selectElement.value;
        console.log(` Dropdown ${index + 1}: Selected testID -> "${testId}"`);

        testIds.push(testId);

        if (testId === "") {
            console.error(`⚠ Dropdown ${index + 1}: Empty selection detected.`);
            allTestsSelected = false;
        }
    });

    console.log("📋 Final selected test IDs:", testIds);

    if (!allTestsSelected) {
        alert("Please select a test for each entry.");
        return;
    }

    // Continue with form submission
    fetch('/api/LabOrder/orderRequest', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            appointmentID: appointmentId,
            userID: userId,
            'urgency[]': urgency,
            'testName[]': testIds
        })
    })
        .then(response => {
            if (response.ok) {
                closeLabReportsModal();
                alert("Lab order created successfully!");
            } else {
                console.error("Error creating lab order:", response);
                alert("Error creating lab order. Please try again.");
            }
        })
        .catch(error => {
            console.error("Error creating lab order:", error);
            alert("An error occurred. Please try again later.");
        });
}


function completeAppointment(appointmentId) {
    fetch('/appointment/complete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            appointmentID: appointmentId
        })
    })
        .then(response => response.text())
        .then(message => {
            alert(message); // Or a better way to display the message
            // Optionally, refresh the appointment list

        })
        .catch(error => {
            console.error("Error completing appointment:", error);
            alert("An error occurred. Please try again.");
        });
}