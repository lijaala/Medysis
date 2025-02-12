document.addEventListener('DOMContentLoaded', () => {
    const labRequestTable = document.getElementById('labRequestTable');
    const modal = document.getElementById('labDetailsModal');

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
                            <td class="actions">
                                <button type="button" class="secondary view-details" data-order-id="${order.orderID}">View Details</button>
                                <button type="button" class="primary" data-order-id="${order.orderID}">Add Results</button>
                                
                            </td>
                        `;

                        // Add event listener after the row is created (more efficient)
                        const viewButton = row.querySelector('.view-details'); // Select the button in the current row
                        viewButton.addEventListener('click', handleViewDetailsClick);

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

        modalBody.innerHTML = '';

        if (labOrderDetails && labOrderDetails.labResults && labOrderDetails.labResults.length > 0) {
            labOrderDetails.labResults.forEach(test => {
                const testNameDiv = document.createElement('div');
                testNameDiv.textContent = test.testName;
                modalBody.appendChild(testNameDiv);
            });
        } else {
            modalBody.textContent = "No tests found for this lab order.";
        }



        modal.style.display = 'flex';



    }




    displayLabOrders();
});