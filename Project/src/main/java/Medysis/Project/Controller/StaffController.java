package Medysis.Project.Controller;


import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private StaffRepository staffRepository;
    @GetMapping("/all")
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();

    }
    @PutMapping("/edit/{staffID}")
    public ResponseEntity<Staff> updateStaffAvailability(
            @PathVariable String staffID,
            @RequestBody Staff updatedStaff) {
        try {
            Staff updated = staffService.updateStaffAvailability(staffID, updatedStaff);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/current")
    public ResponseEntity<?> getLoggedStaff(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null || staffId.isEmpty()) {
            return ResponseEntity.status(401).body("Staff not logged in");
        }

        Optional<Staff> staffOptional = staffRepository.findById(staffId);

        if (staffOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Staff not found");
        }

        Staff staff = staffOptional.get();

        // Ensure the profile picture path is properly prefixed
        String profilePicturePath = staff.getImage();
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            profilePicturePath = "/image/" + profilePicturePath; // Add the image path prefix
        }

        Map<String, Object> response = new HashMap<>();
        response.put("name", staff.getStaffName());
        response.put("profilePicture", profilePicturePath);
        response.put("role", session.getAttribute("userRole"));

        return ResponseEntity.ok(response);
    }
    @GetMapping("getProfile")
    public ResponseEntity<Staff> getProfile(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Staff staff = staffService.getProfile(staffId);
        return ResponseEntity.ok(staff);
    }
    @PutMapping("/update")
    public ResponseEntity<Staff> updateProfile(@RequestBody Staff updatedStaff, HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Staff savedStaff = staffService.updateProfile(staffId, updatedStaff);
        return ResponseEntity.ok(savedStaff);
    }

    @PostMapping("/update-profile-picture")
    public ResponseEntity<?> updateProfilePicture(@RequestParam("photo") MultipartFile photo, HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        boolean isUpdated = staffService.updateProfilePicture(staffId, photo);

        if (isUpdated) {
            session.invalidate(); // ✅ Logout user after profile picture update
            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture updated successfully. Redirecting to login...",
                    "redirectUrl", "/login"
            ));        } else {
            return ResponseEntity.status(500).body(Map.of("message", "Profile picture update failed."));
        }

    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String currentPassword,
                                           @RequestParam String newPassword,
                                           HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        boolean isReset = staffService.resetPassword(staffId, currentPassword, newPassword);

        if (isReset) {
            session.invalidate(); // ✅ Logout user after password reset
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successful. Redirecting to login...",
                    "redirectUrl", "/login"
            ));        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect current password or update failed."));
        }
    }


}
