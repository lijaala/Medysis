package Medysis.Project.Controller;


import Medysis.Project.Repository.AvailabilityRepository;
import Medysis.Project.Service.RoleService;
import Medysis.Project.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AvailabilityRepository availabilityRepository;


}
