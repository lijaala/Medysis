document.addEventListener("DOMContentLoaded",function(){
    fetchRoles();
    fetchStaffData();
})

async function fetchRoles(){
    try{
        const response=await fetch('api/roles');
        console.log(response);
        if(!response.ok){
            throw new Error ("Failed to fetch roles");

        }
        const roles=await response.json();
        populateRolesDropdown(roles);


    }
    catch(error){
        console.log('Error fetching roles:', error)
    }
}

function populateRolesDropdown(roles){
    const roleSelect=document.querySelector('.role');

    roles.forEach(role=>{
        console.log(roles);
        const option=document.createElement('option');
        option.value=role.roleID;
        option.textContent=role.role;
        roleSelect.appendChild(option);
    })
}

function openAddStaffForm(){
    const staffForm=document.getElementById("addStaffModal");
    staffForm.style.display="flex";

}
function closeStaffModal(){
    const staffForm=document.getElementById("addStaffModal");
    staffForm.style.display="none";

}

function openEditStaffModal(staffID) {
    fetch(`http://localhost:8081/api/staff/all`)
        .then(response => response.json())
        .then(data => {
            const staff = data.find(s => s.staffID === staffID);
            if (staff) {
                document.getElementById("editStaffID").value = staffID;

                // Ensure correct time format (HH:mm)
                const startTime = staff.startTime ? staff.startTime.substring(0, 5) : "";
                const endTime = staff.endTime ? staff.endTime.substring(0, 5) : "";

                // Set values and trigger reflow
                const startTimeInput = document.getElementById("editStartTime");
                const endTimeInput = document.getElementById("editEndTime");

                startTimeInput.value = startTime;
                endTimeInput.value = endTime;

                // Force visibility update (in case styles hide it)
                startTimeInput.style.display = "inline-block";
                endTimeInput.style.display = "inline-block";

                console.log("Start Time Set:", startTime);
                console.log("End Time Set:", endTime);

                // Show the modal after setting values
                document.getElementById("editStaffModal").style.display = "flex";
            }
        })
        .catch(error => console.error("Error fetching staff data:", error));
}




function closeEditStaffModal(){
    const staffForm=document.getElementById("editStaffModal");
    staffForm.style.display="none";

}

function fetchStaffData() {
    fetch("http://localhost:8081/api/staff/all") // Replace with your actual API URL
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch staff data");
            }
            return response.json();
        })
        .then(data => {
            populateTable(data);
        })
        .catch(error => {
            console.error("Error fetching staff data:", error);
        });
}

function populateTable(staffList) {
    const tableBody = document.getElementById("staffTableBody");
    tableBody.innerHTML = ""; // Clear existing table data



    staffList.forEach(staff => {
        const row = document.createElement("tr");
        row.setAttribute('data-id', staff.staffID);

        let editButton="";

        if (staff.role && staff.role.role === "ROLE_DOCTOR") {
            editButton = `
                <button class="edit" onclick="openEditStaffModal('${staff.staffID}')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                        <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                            <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
                        </g>
                    </svg>
                </button>
            `;
        }
        else{
            editButton=`<button class="edit"></button>`
        }

        let deleteButton = `
            <button class="delete" onclick="deleteStaff('${staff.staffID}')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M18 19a3 3 0 0 1-3 3H8a3 3 0 0 1-3-3V7H4V4h4.5l1-1h4l1 1H19v3h-1zM6 7v12a2 2 0 0 0 2 2h7a2 2 0 0 0 2-2V7zm12-1V5h-4l-1-1h-3L9 5H5v1zM8 9h1v10H8zm6 0h1v10h-1z" stroke-width="0.5" stroke="currentColor"/></svg>
            </button>
        `;

        row.innerHTML = `
            <td>${staff.staffName}</td>
            <td>${staff.role ? staff.role.role : "N/A"}</td>
            <td>${staff.lastActive ? formatDateTime(staff.lastActive) : "Never"}</td>
            <td>${formatDateTime(staff.addedOn)}</td>
            <td class="actions">
                
                ${deleteButton}
                ${editButton}  

                
            </td>
        `;

        tableBody.appendChild(row);
    });
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return "N/A";
    const date = new Date(dateTimeString);
    return date.toLocaleString();
}
// Open the edit staff modal and populate it with existing data




// Function to submit updated staff data




