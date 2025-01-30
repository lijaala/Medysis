function startAppointment(appointmentID) {
    console.log("Start Appointment triggered for ID:", appointmentID);  // Check if the function is triggered

    // Fetch the appointment row based on the appointment ID
    const appointmentRow = document.querySelector(`tr[data-id='${appointmentID}']`);

    if (appointmentRow) {
        // Get the userID from the data attribute
        const userID = appointmentRow.dataset.userId;

        if (userID) {
            // Proceed to open the diagnosis modal with userID and appointmentID
            openDiagnosisModal(userID, appointmentID);
        } else {
            console.error('User ID not found');
        }
    } else {
        console.error(`Appointment row not found for ID: ${appointmentID}`);
    }
}

function openDiagnosisModal(userID, appointmentID) {
    const diagnosisModal = document.getElementById('diagnosisModal');
    const diagnosisInput = document.getElementById('diagnosisInput');
    const saveDiagnosisButton = document.getElementById('saveDiagnosisButton');

    // Store the appointment and user IDs in the modal data attributes
    diagnosisModal.dataset.appointmentId = appointmentID;
    diagnosisModal.dataset.userId = userID;

    // Show the modal
    diagnosisModal.style.display = 'flex';

    // Add event listener for the save button
}

function saveDiagnosis(userID, appointmentID, diagnosis) {
    const data = new URLSearchParams();
    data.append('userID', userID);
    data.append('appointmentID', appointmentID);
    data.append('diagnosis', diagnosis);

    // Send the diagnosis to the server
    fetch('/appointment/saveDiagnosis', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: data.toString(),
    })
        .then(response => response.json())
        .then(data => {
            console.log('Diagnosis saved successfully:', data);
            closeDiagnosisModal();
        })
        .catch(error => {
            console.error('Error saving diagnosis:', error);
        });
}

function closeDiagnosisModal() {
    const diagnosisModal = document.getElementById('diagnosisModal');
    diagnosisModal.style.display = 'none';
}
