package Medysis.Project.Controller;


import Medysis.Project.Model.Role;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.AvailabilityRepository;
import Medysis.Project.Repository.RoleRepository;
import Medysis.Project.Service.RoleService;
import Medysis.Project.Service.StaffService;
import Medysis.Project.Service.UploadImageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private StaffService staffService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    public  final UploadImageService uploadImageService;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    public AdminController( StaffService staffService, RoleService roleService, AvailabilityRepository availabilityRepository ) {
        this.staffService = staffService;
        this.roleService = roleService;
        this.availabilityRepository = availabilityRepository;
        this.uploadImageService = new UploadImageService();

    }

    @PostMapping("/addStaff")

    public String addStaff(@RequestParam String staffName,
                           @RequestParam String staffEmail,
                           @RequestParam String staffPhone,
                           @RequestParam String staffAddress,
                           @RequestParam String gender,
                           @RequestParam Integer age,
                           @RequestParam MultipartFile image,
                           @RequestParam Integer role,
                           HttpSession session

    ){
        String userRole=(String) session.getAttribute("userRole");
        if (userRole==null ||!userRole.equals("ROLE_ADMIN")){
            return "Access denied ";
        }
        try{
            Staff staff=new Staff();
            staff.setStaffName(staffName);
            staff.setStaffEmail(staffEmail);
            staff.setStaffPhone(staffPhone);
            staff.setStaffAddress(staffAddress);
            staff.setGender(gender);
            staff.setAge(age);
            String imageUrl=uploadImageService.saveImage(image);
            staff.setImage(imageUrl);
            Role roleId=roleRepository.findById(role).orElseThrow(()-> new RuntimeException("Role not found"));
            staff.setRole(roleId);
            staffService.save(staff);
            return "Staff added sucessfully";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/getRoles")
    public List<Role> getAllRoles(){
        return roleService.getAllRoles();

    }
}
