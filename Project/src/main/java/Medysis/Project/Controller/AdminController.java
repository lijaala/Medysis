package Medysis.Project.Controller;


import Medysis.Project.Model.Role;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.RoleRepository;
import Medysis.Project.Service.RoleService;
import Medysis.Project.Service.StaffService;
import Medysis.Project.Service.UploadImageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private StaffService staffService;
    @Autowired
    private RoleService roleService;
    @Autowired
    public final UploadImageService uploadImageService;
    @Autowired
    private RoleRepository roleRepository;



    @Autowired
    public AdminController(StaffService staffService, RoleService roleService) {
        this.staffService = staffService;
        this.roleService = roleService;
        this.uploadImageService = new UploadImageService();

    }

    @PostMapping("/addStaff")
    public ResponseEntity<String> addStaff(@RequestParam String staffName,
                           @RequestParam String staffEmail,
                           @RequestParam String staffPhone,
                           @RequestParam String staffAddress,
                           @RequestParam String gender,
                           @RequestParam Integer age,
                           @RequestParam MultipartFile image,
                           @RequestParam Integer role,
                           @RequestParam(required=false) String startTime,
                           @RequestParam(required=false) String endTime,
                           HttpSession session

    ) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        String adminId = session.getAttribute("userId").toString();

        try {
            String imageUrl = uploadImageService.saveImage(image);
            String staffId = staffService.save(
                    staffName, staffEmail, staffPhone, staffAddress, gender, age,
                    imageUrl, role, startTime, endTime, adminId
            );
            return ResponseEntity.ok("Staff added successfully. Generated Staff ID: " + staffId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: An unexpected error occurred");
        }
    }



}
