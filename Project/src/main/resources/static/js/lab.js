document.addEventListener('DOMContentLoaded', () => {
    const labRequestTable = document.getElementById('labRequestTable');

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
                            <td>${order.userID?.firstName || 'Unknown'} ${order.userID?.lastName || 'Patient'}</td>
                            <td>${order.doctorID?.staffName || 'Unknown Doctor'}</td>
                            <td>${new Date(order.orderDate).toLocaleDateString() || 'N/A'}</td>  
                            <td>${order.labResults?.length || 0}</td>
                            <td>${order.urgency || 'N/A'}</td>
                            <td class="actions">
                                <button type="button" class="view-details" data-order-id="${order.orderID}">View Details</button>
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
        console.log("View details clicked for order ID:", orderId);
        displayLabOrderDetails(orderId);
    }

    // ... (displayLabOrderDetails function - same as before)

    displayLabOrders();
});