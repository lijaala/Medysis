package Medysis.Project.Controller;


import Medysis.Project.Model.Availability;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

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
    public final UploadImageService uploadImageService;
    @Autowired
    private RoleRepository roleRepository;



    @Autowired
    public AdminController(StaffService staffService, RoleService roleService, AvailabilityRepository availabilityRepository) {
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
                           @RequestParam(required=false) String startTime,
                           @RequestParam(required=false) String endTime,
                           HttpSession session

    ) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ROLE_ADMIN")) {
            return "Access denied ";
        }
        Staff staff;
        try {
            staff = new Staff();
            staff.setStaffName(staffName);
            staff.setStaffEmail(staffEmail);
            staff.setStaffPhone(staffPhone);
            staff.setStaffAddress(staffAddress);
            staff.setGender(gender);
            staff.setAge(age);
            String imageUrl = uploadImageService.saveImage(image);
            staff.setImage(imageUrl);


            Role roleId = roleRepository.findById(role).orElseThrow(() -> new RuntimeException("Role not found"));
            staff.setRole(roleId);
            staffService.save(staff);

            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[HH:mm:ss][HH:mm]");
                try {
                    LocalTime start_Time = LocalTime.parse(startTime, formatter);
                    LocalTime end_Time = LocalTime.parse(endTime, formatter);
                    if (end_Time.isBefore(start_Time)) {
                        return "End time cannot be before start time";
                    }
                    staff.setStartTime(start_Time);
                    staff.setEndTime(end_Time);

                } catch (DateTimeParseException e) {
                    return "Invalid time format";
                }

                staffService.save(staff);
                return "Staff added successfully. Generated Staff ID: " + staff.getStaffID();

            }


        } catch (Exception e) {
            return "Error:" + e.getMessage();
        }

        return "Staff added successfully. Generated Staff ID: " + staff.getStaffID();


    }
    @GetMapping("/getRoles")
    public List<Role> getAllRoles () {
        return roleService.getAllRoles();

    }
}
