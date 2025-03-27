let currentLabOrderForResults = null;
document.addEventListener('DOMContentLoaded', () => {
    const labRequestTable = document.getElementById('labRequestTable');
    const modal = document.getElementById('labDetailsModal');
    const labResultsModal = document.getElementById('labResults');
    const closeLabResults= labResultsModal.querySelector('.closeLabResults');
    const submitButton = document.getElementById('submitSingleResult'); // Assuming a global submit button for all


    const closeSpan = document.createElement('span'); // Create a span element
    closeSpan.innerHTML = '&times;';
    closeSpan.style.color="#07104e"
    closeSpan.classList.add('close'); // Add a class for styling
    closeSpan.addEventListener('click', () => {
        modal.style.display = 'none';
        const overlay = document.getElementById('overlay'); // Get the overlay element
        overlay.classList.remove('blur'); // Remove blur first
        overlay.style.display = 'none';
    });

    modal.appendChild(closeSpan);

    function displayLabOrders() {
        fetch('/api/LabOrder/all')
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(labOrders => {
                labRequestTable.innerHTML = ''; // Clear existing table data

                if (Array.isArray(labOrders) && labOrders.length > 0) {
                    labOrders.forEach(order => {
                        const row = labRequestTable.insertRow();
                        row.setAttribute('data-id', order.orderID);

                        row.innerHTML = `
                            <td>${order.orderID}</td>
                            <td>${order.userName || 'Unknown'}</td>

                            <td>${order.doctorName||'Unknown'}</td>
                            <td>${order.orderDate}</td>  
                            <td>${order.labResults?.length || 0}</td>
                            <td>${(order.urgency || 'N/A').toUpperCase()}</td>
                            <td>${(order.labStatus)}</td>
                            <td class="actions">
                                <button type="button" class="secondary view-details" data-order-id="${order.orderID}">View Details</button>
                                <button type="button" class="primary add-results" data-order-id="${order.orderID}">Add Results</button>
                                
                            </td>
                        `;

                        // Add event listener after the row is created (more efficient)
                        row.querySelector('.view-details').addEventListener('click', handleViewDetailsClick);
                        row.querySelector('.add-results').addEventListener('click', handleAddResultsClick);
                    });
                } else {
                    labRequestTable.innerHTML = '<tr><td colspan="7">No lab orders found</td></tr>';
                }
            })
            .catch(error => {
                console.error("Error fetching or processing lab orders:", error);
                labRequestTable.innerHTML = '<tr><td colspan="7">Error fetching lab orders.</td></tr>';
            });
    }

    function handleViewDetailsClick(event) {
        const orderId = event.target.dataset.orderId;
        fetch(`/api/LabOrder/details/${orderId}`)
            .then(response => response.json())
            .then(labOrderDetails => {
                displayLabOrderDetailsInPopover(labOrderDetails, event.target); // Renamed function
            })
            .catch(error => {
                console.error("Error fetching lab order details:", error);
            });
    }
    function displayLabOrderDetailsInPopover(labOrderDetails, button) {
        const modalBody = modal.querySelector('.modal-body');
        const overlay = document.getElementById('overlay'); // Get the overlay element
        overlay.style.display = 'block';
        overlay.classList.add('blur');

        if (!modal || !modalBody) {
            console.error("Modal or modal body element not found!");
            return;
        }

        modalBody.innerHTML = ''; // Clear previous content

        if (labOrderDetails && labOrderDetails.labResults && labOrderDetails.labResults.length > 0) {
            labOrderDetails.labResults.forEach(test => {
                const testContainer = document.createElement('div');
                testContainer.classList.add('test-container'); //

                // Test Name
                const testNameDiv = document.createElement('div');
                testNameDiv.textContent = `Test Name: ${test.testName}`;
                testContainer.appendChild(testNameDiv);

                // Result Value (if available)
                const resultDiv = document.createElement('div');
                resultDiv.textContent = `Result: ${test.resultValue !== null ? test.resultValue : 'Pending'}`;
                testContainer.appendChild(resultDiv);

                // Notes (if available)
                const notesDiv = document.createElement('div');
                notesDiv.textContent = `Notes: ${test.notes ? test.notes : 'No notes available'}`;
                testContainer.appendChild(notesDiv);

                // Append to modal body
                modalBody.appendChild(testContainer);
            });
        } else {
            modalBody.textContent = "No tests found for this lab order.";
        }

        modal.style.display = 'flex';
    }


    function handleAddResultsClick(event) {
        const orderId = event.target.dataset.orderId;
        currentLabOrderForResults = orderId; // Store the orderId

        fetch(`/api/LabOrder/details/${orderId}`)
            .then(response => response.json())
            .then(labOrderDetails => {
                populateLabResultsModal(labOrderDetails);
                showLabResultsModal(); // Ensure modal is visible
            })
            .catch(error => {
                console.error("Error fetching lab order details:", error);
            });
    }
    function populateLabResultsModal(labOrderDetails) {
        if (!labOrderDetails) return;

        // Populate patient and doctor details
        document.getElementById("patientDoc").innerHTML = `
        <span>Patient: ${labOrderDetails.userName || 'Unknown'}</span> <br> <br>
        <span>Doctor: ${labOrderDetails.doctorName || 'Unknown'}</span>
    `;

        // Populate lab test results table
        const labResultTable = document.getElementById("labResultTable");
        labResultTable.innerHTML = ""; // Clear previous entries

        if (labOrderDetails.labResults && labOrderDetails.labResults.length > 0) {
            labOrderDetails.labResults.forEach(result => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>${result.reportId}</td>
                <td>${result.testName}</td>
                <td>${result.measurementUnit || 'N/A'}</td>
                <td>${result.normalRange || 'N/A'}</td>
                <td>
                    <input type="number" class="result-value" 
                        data-test-id="${result.testID}" 
                        value="${result.resultValue !== null ? result.resultValue : ''}" 
                        placeholder="Enter result">
                </td>
                <td>
                    <input type="text" class="result-notes" 
                        value="${result.notes ? result.notes : ''}" 
                        placeholder="Notes">
                </td>
            `;
                labResultTable.appendChild(row);

                // Enable submit button when a result is entered
                row.querySelector('.result-value').addEventListener('input', function() {
                    enableSubmitForEnteredResults();
                });
            });
        } else {
            labResultTable.innerHTML = '<tr><td colspan="6">No tests available.</td></tr>';
        }
    }


    function showLabResultsModal() {
        labResultsModal.style.display = "flex";
    }

    //Close modal when 'x' button is clicked
    closeLabResults.addEventListener('click', () => {
        labResultsModal.style.display = "none";
    });


    function submitLabResults(orderId) {
        const labResults = [];
        const message=document.querySelector(".labResult-message");
        message.innerHTML='';

        // Collect results from inputs (only for tests that have entered data)
        document.querySelectorAll('.result-value').forEach((input, index) => {
            const resultValue = input.value;
            const resultNotes = input.closest('tr').querySelector('.result-notes').value;
            const testId = input.dataset.testId;

            if (resultValue.trim() !== '') { // Only collect results that are not empty
                labResults.push({ orderId, resultValue, resultNotes, testId });
            }
        });

        if (labResults.length === 0) {

            Toastify({
                text: "Please enter at least one result before submitting.",
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
            return;
        }
        let updateCount = 0; // To track the number of successful updates
        let errorCount = 0; // To track errors

        // Send the lab results to the backend as query parameters
        labResults.forEach(result => {
            const { orderId, resultValue, resultNotes, testId } = result;
            console.log(result);

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
                    if (updateCount === labResults.length) {

                        Toastify({
                            text:"Lab results updated successfully!",
                            duration: 3000,
                            backgroundColor: "rgba(200,253,223,0.5)",
                            close: true,
                            gravity: "top",
                            position: "right",
                            style:{

                                color:"rgb(15,94,27)",
                                borderRadius:"8px"
                            }

                        }).showToast();

                        // Delay before closing the modal
                        setTimeout(() => {
                            document.getElementById('labResults').style.display = 'none';
                            displayLabOrders(); // Reload the orders
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



    function enableSubmitForEnteredResults() {
        const resultInputs = document.querySelectorAll('.result-value');
        const resultNotes=document .querySelectorAll('.result-notes');


        const submitButton = document.getElementById('submitSingleResult');  // Assuming there's a global submit button

        // Enable submit button only if any result is entered
        const anyTestEntered = Array.from(resultInputs).some(input => input.value.trim() !== '');
        const anyResultEntered=Array.from(resultNotes).some(input=>input.value.trim()!=='');

        // Only submit filled tests
        if (anyTestEntered || anyResultEntered) {
            submitButton.style.display = 'inline-block';  // Show the submit button when a result is entered
        } else {
            submitButton.style.display = 'none'; // Hide if no result is entered
        }
    }



    if (submitButton) {
        submitButton.addEventListener('click', function () {
            if (!currentLabOrderForResults) {
                console.error('Current Lab Order ID not set!');
                return;
            }

            submitLabResults(currentLabOrderForResults); // Use the stored orderId
            displayLabOrders();
            currentLabOrderForResults = null; // Reset after submission (optional, depending on workflow)
        });
    } else {
        console.error('submitSingleResult button not found!');
    }


    displayLabOrders();
});