package Medysis.Project.Controller;


import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/update/{userID}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable(required = false) Integer userID,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) {

        String editorId = (String) session.getAttribute("userId");
        System.out.println(editorId + name+ phone+ age+ gender+ address+image);

        if (editorId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized: Editor ID not found."));
        }
        if(userID == null) {
            String user=(String) session.getAttribute("userId");
            userID=Integer.parseInt(user);


        }

        try {
            boolean isUpdated = userService.updateUser(userID, name, phone, age, gender, address, image, editorId, weight, bloodType);

            if (isUpdated) {
                return ResponseEntity.ok(Map.of("message", "User updated successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found or update failed."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating user: " + e.getMessage()));
        }
    }
    @GetMapping("/current")
    public ResponseEntity<?> getLoggedPatient(HttpSession session) {
        String patientIdStr = (String) session.getAttribute("userId");

        if (patientIdStr == null || patientIdStr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Patient not logged in");
        }

        try {
            Integer patientId = Integer.parseInt(patientIdStr);
            User patient = userService.getUserById(patientId);

            if (patient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
            }

            String profilePicturePath = patient.getImage();
            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                profilePicturePath = "/image/" + profilePicturePath; // Add the image path prefix
            }

            Map<String, Object> response = new HashMap<>();
            response.put("name", patient.getName());
            response.put("profilePicture", profilePicturePath);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format in session");
        }
    }

    @GetMapping("/{userID}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userID) {
        User user = userService.getUserById(userID);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String currentPassword,
                                           @RequestParam String newPassword,
                                           HttpSession session) {
        String user = (String) session.getAttribute("userId");
        Integer userID = Integer.parseInt(user);
        if (userID == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        boolean isReset = userService.resetPassword(userID, currentPassword, newPassword);

        if (isReset) {
            session.invalidate(); // ✅ Logout user after password reset
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successful. Redirecting to login...",
                    "redirectUrl", "/login"
            ));        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect current password or update failed."));
        }
    }

    @PutMapping("/delete/{userID}")
    public ResponseEntity<Map<String, String>> softDeleteUser(
            @PathVariable Integer userID,
            HttpSession session) {

        String loggedInUserIdStr = (String) session.getAttribute("userId");
        if (loggedInUserIdStr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized: User not logged in."));
        }

        try {
            Integer loggedInUserId = Integer.parseInt(loggedInUserIdStr);
            if (!loggedInUserId.equals(userID)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Forbidden: You can only delete your own account."));
            }

            boolean isDeleted = userService.softDeleteUser(userID);
            if (isDeleted) {
                session.invalidate(); // Logout the user after successful soft delete
                return ResponseEntity.ok(Map.of("message", "Account deleted successfully. Redirecting to login...", "redirectUrl", "/login"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found or deletion failed."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting account: " + e.getMessage()));
        }
    }
    @GetMapping("/profile-info")
    public ResponseEntity<?> getUserProfileInfo(HttpSession session) {
        Integer userId = Integer.parseInt((String)session.getAttribute("userId")) ;

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not logged in."));
        }


        Map<String, Object> profileInfo = userService.getUserProfileDetails(userId);

        if (profileInfo != null) {
            return ResponseEntity.ok(profileInfo);
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "User profile information not found."));
        }
    }


}
