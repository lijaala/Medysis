import {openPrescriptionModal, printPrescription} from "./addDiagnosis";

let labOrderSubmitHandler;
function openLabReportsModal(){
    document.getElementById('orderLabReports').style.display = 'flex';

    document.querySelectorAll(".test-dropdown").forEach(select => populateLabTestDropdown(select));

    const existingDropdowns = document.querySelectorAll(".test-dropdown");
    existingDropdowns.forEach(dropdown => populateLabTestDropdown(dropdown));

    const labOrderForm = document.getElementById('labOrder');
    const prescribeBtn=document.getElementById('saveAndPrescribe');
    const endLabBtn=document.getElementById('saveAndEndLab');

    prescribeBtn.addEventListener('click', saveLabOrderAndPrescribe);
    endLabBtn.addEventListener('click',saveLabOrderAndFinish);


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
const closeLabReports=document.getElementById('closeLabReports');
closeLabReports.addEventListener('click',closeLabReportsModal);

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
const addTestBtn=document.getElementById('addTestBtn');
addTestBtn.addEventListener('click', addLabTest);

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
    if (confirm("Do you want to print the prescription before ending the appointment?")) {
        printPrescription(appointmentId);
    }
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
export {openLabReportsModal,closeLabReportsModal,addLabTest,removeLabTest,populateLabTestDropdown,submitLabOrder,saveLabOrderAndPrescribe,saveLabOrderAndFinish,completeAppointment}
