function viewPastPrescriptions() {
    const userID = document.getElementById("userIdInput").value;

    fetch(`/api/prescriptions/getByUserId?userID=${userID}`)
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("prescriptionsBody");
            tableBody.innerHTML = ""; // Clear previous data

            data.forEach(prescription => {
                const row = document.createElement("tr");

                // Prescription Date
                const dateCell = document.createElement("td");
                dateCell.textContent = prescription.prescriptionDate;
                row.appendChild(dateCell);

                // Medications List
                const medsCell = document.createElement("td");
                medsCell.textContent = prescription.medications.map(med => med.medicationName).join(", ");
                row.appendChild(medsCell);

                // Doctor Name
                const doctorCell = document.createElement("td");
                doctorCell.textContent = prescription.doctorName;
                row.appendChild(doctorCell);

                tableBody.appendChild(row);
            });

            document.getElementById("prescriptionsModal").style.display = "flex";
        })
        .catch(error => console.error("Error fetching past prescriptions:", error));
}

function viewPastLabResults() {
    const userID = document.getElementById("userIdInput").value;

    fetch(`/api/labResults/getByUserId?userID=${userID}`)
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("labResultsBody");
            tableBody.innerHTML = ""; // Clear previous data

            data.forEach(result => {
                const row = document.createElement("tr");

                // Test Name
                const testNameCell = document.createElement("td");
                testNameCell.textContent = result.testName;
                row.appendChild(testNameCell);

                // Result Value
                const resultValueCell = document.createElement("td");
                resultValueCell.textContent = result.resultValue + " " + result.measurementUnit;
                row.appendChild(resultValueCell);

                // Notes
                const notesCell = document.createElement("td");
                notesCell.textContent = result.notes || "N/A";
                row.appendChild(notesCell);

                // Lab Technician
                const technicianCell = document.createElement("td");
                technicianCell.textContent = result.labTechnicianName;
                row.appendChild(technicianCell);

                tableBody.appendChild(row);
            });

            document.getElementById("labResultsModal").style.display = "flex";
        })
        .catch(error => console.error("Error fetching lab results:", error));
}
