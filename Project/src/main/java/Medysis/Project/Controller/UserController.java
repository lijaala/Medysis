package Medysis.Project.Controller;


import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> updateUser(
            @PathVariable Integer userID,
            @RequestBody User updatedUser) {

        boolean isUpdated = userService.updateUser(userID, updatedUser);

        if (isUpdated) {
            return ResponseEntity.ok("User updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }




}
