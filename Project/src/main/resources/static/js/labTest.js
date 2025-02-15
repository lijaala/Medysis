

document.addEventListener("DOMContentLoaded", function () {
    fetch("/api/labTests/availableTests")
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("labTestTable");
            tableBody.innerHTML = ""; // Clear any existing rows

            data.forEach(test => {
                let row = `<tr id="row-${test.testID}">
                            <td>${test.testID}</td>
                            <td>${test.testName}</td>
                            <td>${test.measurementUnit}</td>
                            <td>${test.normalRange}</td>
                            <td class="actions">
                                <button type="button" class="edit"> 
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" onclick="editTest(${test.testID})">
                                    <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                        <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                        <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z"/>
                                    </g>
                                </svg>
                                </button>
                                <button onclick="deleteTest(${test.testID})" class="delete">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                        <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2m-6 5v6m4-6v6"/>
                                    </svg>
                                </button>
                            </td>
                        </tr>`;
                tableBody.innerHTML += row;
            });
        })
        .catch(error => console.error("Error fetching lab tests:", error));
});


function editTest(testID) {
    const row = document.getElementById(`row-${testID}`);
    const cells = row.getElementsByTagName("td");

    // Convert text to input fields for editing
    let testName = cells[1].innerText;
    let measurementUnit = cells[2].innerText;
    let normalRange = cells[3].innerText;

    cells[1].innerHTML = `<input type="text" id="testName-${testID}" value="${testName}" />`;
    cells[2].innerHTML = `<input type="text" id="measurementUnit-${testID}" value="${measurementUnit}" />`;
    cells[3].innerHTML = `<input type="text" id="normalRange-${testID}" value="${normalRange}" />`;

    // Change Edit button to Save button
    let actionCell = cells[4];
    actionCell.innerHTML = `
        <button onclick="saveTest(${testID})" class="edit">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 20 20">
                <path fill="currentColor" d="M3 5a2 2 0 0 1 2-2h8.379a2 2 0 0 1 1.414.586l1.621 1.621A2 2 0 0 1 17 6.621V15a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zm2-1a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1v-4.5A1.5 1.5 0 0 1 6.5 10h7a1.5 1.5 0 0 1 1.5 1.5V16a1 1 0 0 0 1-1V6.621a1 1 0 0 0-.293-.707l-1.621-1.621A1 1 0 0 0 13.379 4H13v2.5A1.5 1.5 0 0 1 11.5 8h-4A1.5 1.5 0 0 1 6 6.5V4zm2 0v2.5a.5.5 0 0 0 .5.5h4a.5.5 0 0 0 .5-.5V4zm7 12v-4.5a.5.5 0 0 0-.5-.5h-7a.5.5 0 0 0-.5.5V16z" stroke-width="0.5" stroke="currentColor"/>
            </svg>
        </button>
        <button onclick="cancelEdit(${testID}, '${testName}', '${measurementUnit}', '${normalRange}')" class="delete">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                <path fill="currentColor" d="m12 12.708l-5.246 5.246q-.14.14-.344.15t-.364-.15t-.16-.354t.16-.354L11.292 12L6.046 6.754q-.14-.14-.15-.344t.15-.364t.354-.16t.354.16L12 11.292l5.246-5.246q.14-.14.345-.15q.203-.01.363.15t.16.354t-.16.354L12.708 12l5.246 5.246q.14.14.15.345q.01.203-.15.363t-.354.16t-.354-.16z" stroke-width="0.5" stroke="currentColor"/>
            </svg>
        </button>
    `;
}
function saveTest(testID) {
    let updatedTest = {
        testName: document.getElementById(`testName-${testID}`).value,
        measurementUnit: document.getElementById(`measurementUnit-${testID}`).value,
        normalRange: document.getElementById(`normalRange-${testID}`).value
    };

    fetch(`/api/labTests/update/${testID}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(updatedTest)
    })
        .then(response => {
            if (response.ok) {
                alert("Test updated successfully!");
                location.reload();
            } else {
                alert("Failed to update test.");
            }
        })
        .catch(error => console.error("Error updating test:", error));
}

function cancelEdit(testID, oldName, oldUnit, oldRange) {
    const row = document.getElementById(`row-${testID}`);
    const cells = row.getElementsByTagName("td");

    // Restore original values
    cells[1].innerText = oldName;
    cells[2].innerText = oldUnit;
    cells[3].innerText = oldRange;

    // Restore Edit and Delete buttons
    const actionCell = cells[4];
    actionCell.innerHTML = `
        <button type="button" class="edit" onclick="editTest(${testID})"> 
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                    <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z"/>
                </g>
            </svg>
        </button>
        <button onclick="deleteTest(${testID})" class="delete">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2m-6 5v6m4-6v6"/>
            </svg>
        </button>

    `;
}

function openTest() {
    document.getElementById('addTest').style.display = 'flex';
}

function closeTest() {
    document.getElementById('addTest').style.display = 'none';
}


document.getElementById('labTest').addEventListener('submit', function(event) {
    event.preventDefault();  // Prevent the default form submission

    // Create a new FormData object from the form
    const formData = new FormData(event.target);

    // Send the form data via a fetch request
    fetch(event.target.action, {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if (!response.ok) {
                // Display error message if response is not OK
                document.querySelector('.labTest-message').innerHTML = 'Error: Unable to add lab test.';
            } else {
                // Display success message
                const message= document.querySelector('.labTest-message');
                message.innerHTML = 'Lab test added successfully!';
                message.className="message";

                // Delay the closing of the modal to ensure user can read the message
                setTimeout(() => {
                    closeTest();  // Close the modal after 2 seconds
                }, 2000);
            }
        })
        .catch(error => {
            // Handle network or other errors
            document.querySelector('.labTest-message').innerHTML = 'Network error, please try again.';
        });
});
/**function deleteTest(testID) {
    if (confirm("Are you sure you want to delete this test?")) {
    fetch(`/api/labTests/delete/${testID}`, {
          method: "DELETE"
        })
            .then(response => {
                if (response.ok) {
                    alert("Test deleted successfully!");
                    location.reload(); // Refresh table to reflect changes
                } else {
                    alert("Failed to delete test!");
                }
            })
            .catch(error => console.error("Error deleting test:", error));
    }
}*/
