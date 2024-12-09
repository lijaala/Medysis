package Medysis.Project.Controller;

import Medysis.Project.Model.Role;
import Medysis.Project.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;
    @GetMapping
    public List<Role> getAllRoles() {
        List<Role> roles=roleService.getAllRoles();
        return roles;
    }

}

