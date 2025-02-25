
function labTech(){
    fetch('/api/dashboard/lab-tech')
        .then(response => response.json())
        .then(data => {
            console.log("Fetched data:", data); // Debugging step
            if (data.totalLabTests != undefined) {
                document.getElementById('totalTests').textContent = data.totalLabTests;

            } else {
                console.error("Total Tests is missing in API response.");
            }
            if (data.pendingLabRequests !== undefined) {
                document.getElementById('pendingRequest').textContent = data.pendingLabRequests;
            } else {
                console.error("Pending Requests  is missing in API response.");
            }
            if (data.urgentPendingLabRequestsNumber !== undefined) {
                document.getElementById("urgentPending").textContent = data.urgentPendingLabRequestsNumber;
            } else {
                console.error("Pending Requests  is missing in API response.");
            }
            if (data.urgentPendingLabRequests!==undefined){
                populateUrgentLabTestsTable(data.urgentPendingLabRequests)
            }
        })
}
function populateUrgentLabTestsTable(labRequest) {
    const tableBody = document.getElementById("urgentLabOrdersTableBody");
    tableBody.innerHTML = ""; // Clear existing data

    labRequest.forEach(order => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${order.orderID}</td>
            <td>${order.userName}</td>
            <td>${order.doctorName}</td>
            <td>${order.orderDate}</td>
            <td class="actions">
                                <button type="button" class="secondary view-details" data-order-id="${order.orderID}">View Details</button>
                                <button type="button" class="primary add-results" data-order-id="${order.orderID}">Add Results</button>
                                
                            </td>
        `;
        row.querySelector('.view-details').addEventListener('click', openUrgentDetails);
        row.querySelector('.add-results').addEventListener('click', urgentResults);


        tableBody.appendChild(row);
    });
}
const urgentDetailsModal=document.getElementById("urgentLabDetailsModal");
const urgentDetailsBody=document.getElementById("urgentDetails");

const urgentLabResults=document.getElementById("urgentLabResults");

function openUrgentDetails(event){

    const orderId=event.target.dataset.orderId;
    fetch(`/api/LabOrder/details/${orderId}`)
        .then(response => response.json())
        .then(urgentDetails => {
            displayUrgentDetails(urgentDetails, event.target); // Renamed function
        })
        .catch(error => {
            console.error("Error fetching lab order details:", error);
        });

}
function displayUrgentDetails(urgentDetails, button){
    urgentDetailsModal.style.display="flex";
    urgentDetailsBody.innerHTML="";
    if(urgentDetails && urgentDetails.labResults && urgentDetails.labResults.length>0){
        urgentDetails.labResults.forEach(test=>{
            const testList=document.createElement('div');
            testList.textContent=test.testName;
            urgentDetailsBody.appendChild(testList);
        })

    }
    else{
        urgentDetailsBody.textContent="No tests found for this lab order";
    }

}
function closeUrgentDetailsModal(){
    urgentDetailsModal.style.display="none";

}

function urgentResults(event){
    const orderId = event.target.dataset.orderId;

    fetch(`/api/LabOrder/details/${orderId}`)
        .then(response => response.json())
        .then(labOrderDetails => {
            populateUrgentLabResultsModal(labOrderDetails);
            showUrgentResultsModal(); // Ensure modal is visible
        })
        .catch(error => {
            console.error("Error fetching lab order details:", error);
        });
}

function populateUrgentLabResultsModal(urgentDetails) {
    if (!urgentDetails) return;

    document.getElementById("urgentPatientDoc").innerHTML = `
            <span>Patient: ${urgentDetails.userName || 'Unknown'}</span> <br> <br>
            <span>Doctor: ${urgentDetails.doctorName || 'Unknown'}</span>
        `;

    // Populate lab test results table
    const urgentLabResultTable = document.getElementById("UrgentLabResultTable");
    urgentLabResultTable.innerHTML = ""; // Clear previous entries

    if (urgentDetails.labResults && urgentDetails.labResults.length > 0) {
        urgentDetails.labResults.forEach(result => {
            const row = document.createElement('tr');
            row.innerHTML = `
                    <td>${result.reportId}</td>
                    <td>${result.testName}</td>
                    <td>${result.measurementUnit || 'N/A'}</td>
                    <td>${result.normalRange || 'N/A'}</td>
                    <td><input type="number" class="result-value" data-test-id="${result.testID}" placeholder="Enter result"></td>
                    <td><input type="text" class="result-notes" placeholder="Notes"></td>
                `;
            urgentLabResultTable.appendChild(row);

            row.querySelector('.result-value').addEventListener('input', function() {
                enableSubmitForEnteredResults(); // Enable submission when a result is entered
            });
        });
    } else {
        urgentLabResultTable.innerHTML = '<tr><td colspan="6">No tests available.</td></tr>';
    }
}
function showUrgentResultsModal() {
    urgentLabResults.style.display = "flex";
}

function closeUrgentResultsModal(){
    urgentLabResults.style.display="none";
}

function submitUrgentLabResults(orderId) {
    const urgentLabResults = [];
    const message=document.querySelector(".labResult-message");
    message.innerHTML='';

    // Collect results from inputs (only for tests that have entered data)
    document.querySelectorAll('.result-value').forEach((input, index) => {
        const resultValue = input.value;
        const resultNotes = input.closest('tr').querySelector('.result-notes').value;
        const testId = input.dataset.testId;

        if (resultValue.trim() !== '') { // Only collect results that are not empty
            urgentLabResults.push({ orderId, resultValue, resultNotes, testId });
        }
    });

    if (urgentLabResults.length === 0) {
        alert('Please enter at least one result before submitting.');
        return;
    }
    let updateCount = 0; // To track the number of successful updates
    let errorCount = 0; // To track errors

    // Send the lab results to the backend as query parameters
    urgentLabResults.forEach(result => {
        const { orderId, resultValue, resultNotes, testId } = result;

        // Construct the URL with query parameters
        const url = `/labResult/update/${orderId}/${testId}?resultValue=${resultValue}&notes=${resultNotes}`;

        // Send the PUT request
        fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            }
        })
            .then(response => response.text())
            .then(data => {
                updateCount++;
                if (updateCount === urgentLabResults.length) {
                    message.innerHTML = 'Lab results updated successfully!';

                    // Delay before closing the modal
                    setTimeout(() => {
                        closeUrgentResultsModal()
                        labTech() // Reload the orders
                    }, 2000); // 2-second delay for readability
                }
            })
            .catch(error => {
                console.error('Error updating lab results:', error);
                errorCount++;

                // Display error message only once
                if (errorCount === 1) {
                    message.innerHTML = '<span style="color: red;">Failed to update some lab results. Please try again.</span>';
                }


            });
    });
}
const urgentResultInputs = document.querySelectorAll('.result-value');
const urgentSubmitButton = document.getElementById('urgentSingleResult');  // Assuming there's a global submit button



function enableSubmitForEnteredResults() {
    const anyTestEntered = Array.from(urgentResultInputs).some(input => input.value.trim() !== '');
        // Only submit filled tests
    if (anyTestEntered) {
        urgentSubmitButton.style.display = 'inline-block';  // Show the submit button when a result is entered
    } else {
        urgentSubmitButton.style.display = 'none'; // Hide if no result is entered
    }
}



if (urgentSubmitButton) {
    urgentSubmitButton.addEventListener('click', function () {
        const orderId = document.querySelector('.add-results').dataset.orderId;  // Get the order ID from the button

        if (!orderId) {
            console.error('Order ID not found!');
            return;
        }

        submitUrgentLabResults(orderId);  // Pass the orderId directly to the function
        labTech();  // Reload lab orders
    });
} else {
    console.error('submitSingleResult button not found!');
}

