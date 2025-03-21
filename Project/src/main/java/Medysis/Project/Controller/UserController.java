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
            @PathVariable Integer userID,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) {

        String editorId = (String) session.getAttribute("userId");
        System.out.println(editorId + name+ phone+ age+ gender+ address+image);

        if (editorId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized: Editor ID not found."));
        }

        try {
            boolean isUpdated = userService.updateUser(userID, name, phone, age, gender, address, image, editorId);

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




}
