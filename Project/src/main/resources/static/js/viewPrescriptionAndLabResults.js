function viewPastPrescriptions() {
    const userId = document.getElementById("userIdInput").value;

    fetch(`/api/prescriptions/getByUserId?userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            console.log(data);
            const tableBody = document.getElementById("prescriptionsBody");
            tableBody.innerHTML = ""; // Clear previous data

            data.forEach(prescription => {
                prescription.medications.forEach(medication => {
                    const row = document.createElement("tr");

                    // Prescription Date (only for the first medication of each prescription)
                    const dateCell = document.createElement("td");
                    dateCell.textContent = prescription.prescriptionDate;
                    row.appendChild(dateCell);

                    // Prescribed By
                    const doctorCell = document.createElement("td");
                    doctorCell.textContent = prescription.prescribedBy;
                    row.appendChild(doctorCell);

                    // Medication Name
                    const medNameCell = document.createElement("td");
                    medNameCell.textContent = medication.medicationName;
                    row.appendChild(medNameCell);

                    // Dosage
                    const dosageCell = document.createElement("td");
                    dosageCell.textContent = medication.dosage;
                    row.appendChild(dosageCell);

                    // Interval
                    const intervalCell = document.createElement("td");
                    intervalCell.textContent = medication.medicationInterval;
                    row.appendChild(intervalCell);

                    // Duration
                    const durationCell = document.createElement("td");
                    durationCell.textContent = medication.daysOfIntake;
                    row.appendChild(durationCell);

                    tableBody.appendChild(row);
                });
            });

            document.getElementById("viewPrescriptionsModal").style.display = "flex";
        })
        .catch(error => console.error("Error fetching past prescriptions:", error));
}

function viewPastLabResults() {
    const userId = document.getElementById("userIdInput").value;

    fetch(`/api/LabOrder/getByUserId?userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            console.log(data);
            const tableBody = document.getElementById("labResultsBody");
            tableBody.innerHTML = ""; // Clear previous data

            data.forEach(order => {
                order.labResults.forEach(result => {  // ✅ Correctly looping through labResults
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
                    technicianCell.textContent = result.labTechnicianID || "N/A";
                    row.appendChild(technicianCell);

                    tableBody.appendChild(row);
                });
            });

            document.getElementById("viewLabResultsModal").style.display = "flex";
        })
        .catch(error => console.error("Error fetching lab results:", error));
}
function closePrescriptionHistory(){
    const presHistory= document.getElementById("viewPrescriptionsModal");
    presHistory.style.display="none";
}function closeLabResultsHistory(){
    const ResultHis=document.getElementById("viewLabResultsModal");
    ResultHis.style.display="none";
}