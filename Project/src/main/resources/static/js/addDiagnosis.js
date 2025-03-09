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



// save diagnosis
function saveDiagnosis(event, callback) {

    event.preventDefault();

    const form = document.getElementById("diagnosisForm");
    const formData = new FormData(form);


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
                Toastify({
                    text: "Error saving Diagnosis",
                    duration: 1500,
                    backgroundColor: "rgba(200,253,223,0.5)",
                    gravity: "top",
                    position: "right",

                    style:{

                        color:"rgb(15,94,27)",
                        borderRadius:"8px"
                    },onClick: function(){}
                }).showToast();

            } catch (parseError) {
                console.log("Success:", data);
                Toastify({
                    text: "Diagnosis Saved Successfully",
                    duration: 1500,
                    backgroundColor: "rgb(200,253,223)",
                    gravity: "top",
                    position: "right",

                    style:{
                        color:"rgb(15,94,27)",
                        borderRadius:"8px"
                    },onClick: function(){}
                }).showToast();
                if (callback) {
                callback(data);
                }
            }


        })
        .catch(error => {
            console.error("Fetch Error:", error);
            Toastify({
                text: "An error occurred please try again",
                duration: 3000,
                backgroundColor: "rgb(253,200,200)",
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

//open prescription modal and save diagnosis
function saveDiagnosisAndNext(event) {
    const appointmentId = document.getElementById("appointmentIdInput").value;
    const userId = document.getElementById("userIdInput").value;
    openPrescriptionModal(appointmentId,userId);

    saveDiagnosis(event, (message) => {
        if (message === "Success") {
            // Delay closing the diagnosis modal to show the message:
            setTimeout(() => {
                closeDiagnosisModal();
                setTimeout(() => { // Delay opening prescription modal
                    const appointmentId = document.getElementById("appointmentIdInput").value;
                    const userId = document.getElementById("userIdInput").value;

                    openPrescriptionModal(appointmentId,userId);
                }, 3000);
            }, 3500); // Delay for closing (1.5 seconds)
        }
    });
}
function saveDiagnosisAndOrderTest(event){
    saveDiagnosis(event, (message) => {
        if (message === "Success") {
            // Delay closing the diagnosis modal to show the message:

                closeDiagnosisModal();
                setTimeout(() => { // Delay opening prescription modal
                    const appointmentId = document.getElementById("appointmentIdInput").value;
                    const userId = document.getElementById("userIdInput").value;

                    openLabReportsModal(appointmentId,userId);
                }, 1000);
            // Delay for closing (1.5 seconds)
        }
    });
}
// save diagnosis and close
function saveDiagnosisAndClose(event) {
    saveDiagnosis(event, (message) => {
        if (message === "Success") {
            const appointmentId = document.getElementById("appointmentIdInput").value;
            completeAppointment(appointmentId);
            setTimeout(closeDiagnosisModal, 3000);
            // Delay for closing
        }
    });
}
let diagnosisNextHandler, diagnosisDoneHandler, diagnosisLabNextHandler;
let prescriptionNextHandler, prescriptionDoneHandler;
let labOrderSubmitHandler;
//open diagnosis modal
function openDiagnosisModal() {
    const diagnosisModal = document.getElementById("diagnosisModal");

    const nextButton = document.getElementById('nextButton');
    const doneButton = document.getElementById('doneButton');
    const labNext=document.getElementById('orderTest');

    if (!diagnosisModal) {
        console.error("Diagnosis modal not found!");
        return;
    }

    // Simply open the modal
    if (diagnosisNextHandler) {
        nextButton.removeEventListener('click', diagnosisNextHandler);
    }
    if (diagnosisDoneHandler) {
        doneButton.removeEventListener('click', diagnosisDoneHandler);
    }
    if (diagnosisLabNextHandler) {
        labNext.removeEventListener('click', diagnosisLabNextHandler);
    }

    // Define new event listener functions
    diagnosisNextHandler = function (event) {
        saveDiagnosisAndNext(event);
    };
    diagnosisDoneHandler = function (event) {
        saveDiagnosisAndClose(event, () => {
            completeAppointment(document.getElementById("appointmentIdInput").value);
        });
    };
    diagnosisLabNextHandler = function (event) {
        saveDiagnosisAndOrderTest(event);
    };

    // Add new event listeners
    nextButton.addEventListener('click', diagnosisNextHandler);
    doneButton.addEventListener('click', diagnosisDoneHandler);
    labNext.addEventListener('click', diagnosisLabNextHandler);

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


// update the status of medical condition

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
        Toastify({
            text: message,
            duration: 1500,
            backgroundColor: "rgb(200,253,223)",
            gravity: "top",
            position: "right",

            style:{

                color:"rgb(15,94,27)",
                borderRadius:"8px"
            },onClick: function(){}
        }).showToast();
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
        alert("");
        Toastify({
            text: "Appointment ID and User ID are required!",
            duration: 1500,
            backgroundColor: "rgba(253,200,200)",
            gravity: "top",
            position: "right",

            style:{
                color:"rgb(167,6,14)",
                borderRadius:"8px"
            },onClick: function(){}
        }).showToast();
        return;
    }

    const prescriptionNextButton = document.getElementById('prescriptionNext');
    const prescriptionDoneButton = document.getElementById('prescriptionDone');

    if (prescriptionNextHandler) {
        prescriptionNextButton.removeEventListener('click', prescriptionNextHandler);
    }
    if (prescriptionDoneHandler) {
        prescriptionDoneButton.removeEventListener('click', prescriptionDoneHandler);
    }

    // Define new event listener functions
    prescriptionNextHandler = function (event) {
        setTimeout(() => {
            closePrescriptionModal();
            openLabReportsModal(
                document.getElementById("appointmentId").value,
                document.getElementById("userId").value
            );
        }, 5000);
    };
    prescriptionDoneHandler = function (event) {
        setTimeout(() => {
            closePrescriptionModal();
            completeAppointment(document.getElementById("appointmentId").value);
        }, 3000);
    };

    // Add new event listeners
    prescriptionNextButton.addEventListener('click', prescriptionNextHandler);
    prescriptionDoneButton.addEventListener('click', prescriptionDoneHandler);


    document.getElementById('appointmentId').value = appointmentId;
    document.getElementById('userId').value = userId;
    document.getElementById('prescriptionModal').style.display = 'flex';

    const form = document.getElementById('prescriptionForm');

    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        console.log("Submitting prescription data:", data);
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

                Toastify({
                    text: "Days of intake must be a positive number for " + medicationName,
                    duration: 3000,
                    backgroundColor: "rgb(253,200,200)",
                    close: true,
                    gravity: "top",
                    position: "right",
                    borderRadius:"8px",
                    style:{
                        color:"rgb(167,6,14)",
                        borderRadius:"8px"
                    },onClick: function(){}
                }).showToast();
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

                    Toastify({
                        text: "Error!",
                        duration: 3000,
                        backgroundColor: "rgba(253,200,200)",
                        close: true,
                        gravity: "top",
                        position: "right",
                        borderRadius:"8px",
                        style:{
                            color:"rgb(167,6,14)",
                            borderRadius:"8px"
                        },onClick: function(){}
                    }).showToast();
                } catch (parseError) {
                    console.log("Success:", data);
                    Toastify({
                        text: data,
                        duration: 1500,
                        backgroundColor: "rgba(200,253,223,0.5)",
                        gravity: "top",
                        position: "right",

                        style:{

                            color:"rgb(15,94,27)",
                            borderRadius:"8px"
                        },onClick: function(){}
                    }).showToast();
                    closePrescriptionModal();
                }
            })
            .catch(error => {
                console.error("Error saving prescription:", error);


                Toastify({
                    text: "An Error occurred.Please try again later",
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



    }, { once: true });  // Ensure the event doesn't bind multiple times
}


function closePrescriptionModal() {
    document.getElementById('prescriptionModal').style.display = 'none';
}
let medicationCount = 0;

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



    if (labOrderSubmitHandler) {
        labOrderForm.removeEventListener('submit', labOrderSubmitHandler);
    }

    // Define new event listener function
    labOrderSubmitHandler = function (event) {
        event.preventDefault();
        submitLabOrder(() => {
            completeAppointment(document.getElementById("appointmentIdInput").value);
        });
    };

    // Add new event listener
    labOrderForm.addEventListener('submit', labOrderSubmitHandler);


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

    const appointmentIdInput = document.getElementById('appointmentIdInput').value;
    if (!appointmentIdInput) {
        console.error("Appointment ID is missing or empty!");
        alert("Error: Appointment ID is required.");
        return;
    }
    const userId = document.getElementById('userIdInput').value;
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
            console.error(`Dropdown ${index + 1}: Empty selection detected.`);
            allTestsSelected = false;
        }
    });

    console.log("Final selected test IDs:", testIds);

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
            appointmentID: appointmentIdInput,
            userID: userId,
            'urgency[]': urgency,
            'testName[]': testIds
        })
    })
        .then(response => {
            if (response.ok) {
                closeLabReportsModal();
                Toastify({
                    text: "Lab Order Created Successfully",
                    duration: 1500,
                    backgroundColor: "rgba(200,253,223,0.5)",
                    gravity: "top",
                    position: "right",

                    style:{

                        color:"rgb(15,94,27)",
                        borderRadius:"8px"
                    },onClick: function(){}
                }).showToast();
            } else {
                console.error("Error creating lab order:", response);
                Toastify({
                    text: "Error creating lab order. Please try again.",
                    duration: 3000,
                    backgroundColor: "rgb(253,200,200)",
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
        })
        .catch(error => {
            console.error("Error creating lab order:", error);


            Toastify({
                text: "An error occurred. Please try again later.",
                duration: 3000,
                backgroundColor: "rgb(253,200,200)",
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
function saveLabOrderAndPrescribe(){
    submitLabOrder(() => {
        const appointmentId = document.getElementById("appointmentIdInput")?.value;
        const userId = document.getElementById("userIdInput")?.value;

        if (!appointmentId || !userId) {
            console.error("❌ Error: Missing appointmentId or userId.");
            alert("Error: Missing required information.");
            return;
        }


        openPrescriptionModal(appointmentId, userId);
    });
}
function saveLabOrderAndFinish(){
    submitLabOrder(() => {
        const appointmentId = document.getElementById("appointmentIdInput")?.value;
        if (!appointmentId) {
            console.error("❌ Error: Missing appointmentId.");
            alert("Error: Missing required information.");
            return;
        }
        completeAppointment(appointmentId);
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


            Toastify({
                text: message,
                duration: 1500,
                backgroundColor: "rgba(200,253,223,0.5)",
                gravity: "top",
                position: "right",

                style:{

                    color:"rgb(15,94,27)",
                    borderRadius:"8px"
                },onClick: function(){}
            }).showToast();// Or a better way to display the message
            // Optionally, refresh the appointment list

        })
        .catch(error => {
            console.error("Error completing appointment:", error);


            Toastify({
                text: "An error occurred. Please try again.",
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