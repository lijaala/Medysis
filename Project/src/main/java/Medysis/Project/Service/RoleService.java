package Medysis.Project.Service;

import Medysis.Project.Model.Role;
import Medysis.Project.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role findRoleById(Integer roleID) {return roleRepository.findById(roleID).orElse(null);}
    public Role findRoleByName(String roleName) {
        return roleRepository.findByRole("ROLE_"+roleName);
    }
    public List<Role> getAllRoles() {return roleRepository.findAll();}

    public static final Map<Integer, String> roleMap=Map.of(
            1,"Admin",
            2,"Doctor",
            3,"Lab Technician",
            4,"Patients"
    );
    public String getRoleNameById(Integer roleID)
    {return "ROLE_"+roleMap.get(roleID);
    }

}
