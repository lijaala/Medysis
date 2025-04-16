let currentLabOrderForResults = null;
const labRequestTable = document.getElementById('labRequestTable');
const modal = document.getElementById('labDetails');
const modalContent = modal.querySelector('.modal-content');
const labResultsModal = document.getElementById('labResults');
const closeLabResults = labResultsModal.querySelector('.closeLabResults');
const submitButton = document.getElementById('submitSingleResult');
let originalLabOrders = []; // Store original lab orders for search
let currentLabOrders = []; // Store current lab orders for display
let currentDateFilter = '';
let currentUrgencyFilter = '';
let currentSortColumn = null;
let currentSortOrder = 'asc';
document.addEventListener('DOMContentLoaded', () => {
    displayLabOrders();
});

document.getElementById('applyLabFilters').addEventListener('click', () => {
    currentDateFilter = document.getElementById('dateFilter').value;
    currentUrgencyFilter = document.getElementById('urgencyFilter').value.toLowerCase();
    displayLabOrders();
});

function displayLabOrders(sortColumn, sortOrder) {
    if (typeof sortColumn === 'undefined') {
        sortColumn = currentSortColumn;
        sortOrder = currentSortOrder;
    }
    fetch('/api/LabOrder/all')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(labOrders => {
            originalLabOrders = labOrders;
            currentLabOrders = labOrders;

            const searchInput = document.getElementById('search');
            if (searchInput && searchInput.value) {
                searchLabOrders(searchInput.value);
            } else {
                applyAndDisplay(sortColumn, sortOrder); // Use stored sort state
            }
        })
        .catch(error => {
            console.error("Error fetching or processing lab orders:", error);
            labRequestTable.innerHTML = '<tr><td colspan="7">Error fetching lab orders.</td></tr>';
        });
}

function applyAndDisplay(sortColumn, sortOrder) {
    let filteredOrders = applyFilters(currentLabOrders);
    let sortedOrders = applyResultSorting(filteredOrders, sortColumn, sortOrder);
    updateTable(sortedOrders, sortColumn, sortOrder);
}
function updateTable(sortedOrders, sortColumn, sortOrder) {
    labRequestTable.innerHTML = '';
    const headers = document.querySelectorAll('.orderTable thead td[data-sort]');

    // Remove existing event listeners
    headers.forEach(header => {
        header.replaceWith(header.cloneNode(true));
    });

    // Re-select headers after cloning to get the new elements
    const newHeaders = document.querySelectorAll('thead td[data-sort]');

    newHeaders.forEach(header => {
        header.addEventListener('click', () => {
            currentSortColumn = header.getAttribute('data-sort');
            currentSortOrder = header.getAttribute('data-order') || 'asc';
            currentSortOrder = currentSortOrder === 'asc' ? 'desc' : 'asc';
            header.setAttribute('data-order', currentSortOrder);

            applyAndDisplay(currentSortColumn, currentSortOrder);
        });
    });

    if (Array.isArray(sortedOrders) && sortedOrders.length > 0) {
        sortedOrders.forEach(order => {
            const row = labRequestTable.insertRow();
            row.setAttribute('data-id', order.orderID);

            row.innerHTML = `
                <td>${order.orderID}</td>
                <td>${order.userName || 'Unknown'}</td>
                <td>${order.doctorName || 'Unknown'}</td>
                <td>${order.orderDate}</td>
                <td>${order.labResults?.length || 0}</td>
                <td>${(order.urgency || 'N/A').toUpperCase()}</td>
                <td>${(order.labStatus)}</td>
                <td class="actions">
                    <button type="button" class="secondary view-details" data-order-id="${order.orderID}">View Details</button>
                    <button type="button" class="primary add-results" data-order-id="${order.orderID}">Add Results</button>
                </td>
            `;

            row.querySelector('.view-details').addEventListener('click', handleViewDetailsClick);
            row.querySelector('.add-results').addEventListener('click', handleAddResultsClick);
        });
    } else {
        labRequestTable.innerHTML = '<tr><td colspan="7">No lab orders found</td></tr>';
    }
}
function handleViewDetailsClick(event) {
    const orderId = event.target.dataset.orderId;
    fetch(`/api/LabOrder/details/${orderId}`)
        .then(response => response.json())
        .then(labOrderDetails => {
            displayLabOrderDetailsInPopover(labOrderDetails, event.target);
        })
        .catch(error => {
            console.error("Error fetching lab order details:", error);
        });
}

function displayLabOrderDetailsInPopover(labOrderDetails, button) {
    if (!modal || !modalContent) {
        console.error("Modal or modal content element not found!");
        return;
    }

    modal.style.display = 'flex';
    modalContent.innerHTML = "";
    const closeButton = document.createElement('span');
    closeButton.innerHTML = '&times;';
    closeButton.classList.add('close');
    closeButton.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    modalContent.appendChild(closeButton);

    if (labOrderDetails && labOrderDetails.labResults && labOrderDetails.labResults.length > 0) {
        labOrderDetails.labResults.forEach(test => {
            const testContainer = document.createElement('div');
            testContainer.classList.add('test-container');

            const testNameDiv = document.createElement('div');
            testNameDiv.textContent = `Test Name: ${test.testName}`;
            testContainer.appendChild(testNameDiv);

            const resultDiv = document.createElement('div');
            resultDiv.textContent = `Result: ${test.resultValue !== null ? test.resultValue : 'Pending'}`;
            testContainer.appendChild(resultDiv);

            const notesDiv = document.createElement('div');
            notesDiv.textContent = `Notes: ${test.notes ? test.notes : 'No notes available'}`;
            testContainer.appendChild(notesDiv);

            modalContent.appendChild(testContainer);
        });
    } else {
        modalContent.textContent = "No tests found for this lab order.";
    }
}

function handleAddResultsClick(event) {
    const orderId = event.target.dataset.orderId;
    currentLabOrderForResults = orderId;

    fetch(`/api/LabOrder/details/${orderId}`)
        .then(response => response.json())
        .then(labOrderDetails => {
            populateLabResultsModal(labOrderDetails);
            showLabResultsModal();
        })
        .catch(error => {
            console.error("Error fetching lab order details:", error);
        });
}

function populateLabResultsModal(labOrderDetails) {
    if (!labOrderDetails) return;

    document.getElementById("patientDoc").innerHTML = `
        <span>Patient: ${labOrderDetails.userName || 'Unknown'}</span> <br> <br>
        <span>Doctor: ${labOrderDetails.doctorName || 'Unknown'}</span>
    `;

    const labResultTable = document.getElementById("labResultTable");
    labResultTable.innerHTML = "";

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

closeLabResults.addEventListener('click', () => {
    labResultsModal.style.display = "none";
});

function submitLabResults(orderId) {
    const labResults = [];
    const message = document.querySelector(".labResult-message");
    message.innerHTML = '';

    document.querySelectorAll('.result-value').forEach((input, index) => {
        const resultValue = input.value;
        const resultNotes = input.closest('tr').querySelector('.result-notes').value;
        const testId = input.dataset.testId;

        if (resultValue.trim() !== '') {
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
            borderRadius: "8px",
            style: {
                color: "rgb(167,6,14)",
                borderRadius: "8px"
            },
            onClick: function() {}
        }).showToast();
        return;
    }

    let updateCount = 0;
    let errorCount = 0;

    labResults.forEach(result => {
        const { orderId, resultValue, resultNotes, testId } = result;
        const url = `/labResult/update/${orderId}/${testId}?resultValue=${resultValue}&notes=${resultNotes}`;

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
                        text: "Lab results updated successfully!",
                        duration: 3000,
                        backgroundColor: "rgba(200,253,223,0.5)",
                        close: true,
                        gravity: "top",
                        position: "right",
                        style: {
                            color: "rgb(15,94,27)",
                            borderRadius: "8px"
                        }
                    }).showToast();

                    setTimeout(() => {
                        document.getElementById('labResults').style.display = 'none';
                        displayLabOrders();
                    }, 2000);
                }
            })
            .catch(error => {
                console.error('Error updating lab results:', error);
                errorCount++;
                if (errorCount === 1) {
                    message.innerHTML = '<span style="color: red;">Failed to update some lab results. Please try again.</span>';
                }
            });
    });
}

function enableSubmitForEnteredResults() {
    const resultInputs = document.querySelectorAll('.result-value');
    const resultNotes = document.querySelectorAll('.result-notes');
    const submitButton = document.getElementById('submitSingleResult');

    const anyTestEntered = Array.from(resultInputs).some(input => input.value.trim() !== '');
    const anyResultEntered = Array.from(resultNotes).some(input => input.value.trim() !== '');

    if (anyTestEntered || anyResultEntered) {
        submitButton.style.display = 'inline-block';
    } else {
        submitButton.style.display = 'none';
    }
}

if (submitButton) {
    submitButton.addEventListener('click', function() {
        if (!currentLabOrderForResults) {
            console.error('Current Lab Order ID not set!');
            return;
        }

        submitLabResults(currentLabOrderForResults);
        displayLabOrders();
        currentLabOrderForResults = null;
    });
} else {
    console.error('submitSingleResult button not found!');
}

function closeDetailsModal() {
    document.getElementById("labDetails").style.display = "none";
}

function applyFilters(labOrders) {
    return labOrders.filter(order => {
        const matchesDate = !currentDateFilter || order.orderDate === currentDateFilter;
        const matchesUrgency = !currentUrgencyFilter || (order.urgency && order.urgency.toLowerCase() === currentUrgencyFilter);
        return matchesDate && matchesUrgency;
    });
}

function searchLabOrders(query) {
    let searchedOrders = originalLabOrders.filter(order => {
        const patientName = order.userName?.toLowerCase() || '';
        const doctorName = order.doctorName?.toLowerCase() || '';
        const orderId = String(order.orderID).toLowerCase();
        return patientName.includes(query.toLowerCase()) || doctorName.includes(query.toLowerCase()) || orderId.includes(query.toLowerCase());
    });
    currentLabOrders = searchedOrders;
    applyAndDisplay();
}

function applyResultSorting(orders, sortColumn, sortOrder) {
    if (!sortColumn) return orders;

    return orders.slice().sort((a, b) => {
        let valA = a[sortColumn];
        let valB = b[sortColumn];

        if (valA == null) return 1;
        if (valB == null) return -1;

        if (sortColumn === 'orderID') {
            const numA = typeof valA === 'string' ? parseInt(valA, 10) : valA;
            const numB = typeof valB === 'string' ? parseInt(valB, 10) : valB;
            return sortOrder === 'asc' ? numA - numB : numB - numA;
        } else if (sortColumn === 'orderDate'){
            const dateA = new Date(valA);
            const dateB = new Date(valB);
            return sortOrder === 'asc'? dateA - dateB : dateB - dateA;
        }else if (typeof valA === 'string') {
            return sortOrder === 'asc'
                ? valA.localeCompare(valB)
                : valB.localeCompare(valA);
        } else {
            return sortOrder === 'asc' ? valA - valB : valB - valA;
        }
    });
}