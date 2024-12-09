package Medysis.Project.Service;

import Medysis.Project.Model.Role;
import Medysis.Project.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role findRoleById(Integer roleID) {return roleRepository.findById(roleID).orElse(null);}
    public Role findRoleByName(String roleName) {
        return roleRepository.findByRole(roleName);
    }
    public List<Role> getAllRoles() {return roleRepository.findAll();}
}
