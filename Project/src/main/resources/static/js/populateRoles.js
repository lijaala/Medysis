document.addEventListener("DOMContentLoaded",function(){
    fetchRoles();
})

async function fetchRoles(){
    try{
        const response=await fetch('api/admin/getRoles');
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
    const roleSelect=document.getElementById('role');
    roles.forEach(role=>{
        const option=document.createElement('option');
        option.value=role.roleID;
        option.textContent=role.role;
        roleSelect.appendChild(option);
    })
}