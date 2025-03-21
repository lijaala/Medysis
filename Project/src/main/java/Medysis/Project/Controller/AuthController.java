package Medysis.Project.Controller;

import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")


public class AuthController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final EmailService emailService;

    @Autowired
    private final UploadImageService uploadImageService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private AuthService authService;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, EmailService emailService, UploadImageService uploadImageService, StaffService staffService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.uploadImageService = uploadImageService;
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam String password,
                         @RequestParam String email,
                         @RequestParam String address,
                         @RequestParam String phone,
                         @RequestParam Integer age,
                         @RequestParam String gender,
                         @RequestParam MultipartFile image) {
        System.out.println("Received signup request with name: " + name + ", email: " + email + ", password: " + password);

        try{
            User user=new User();
            user.setName(name);
            user.setEmail(email);
            user.setAddress(address);
            user.setPhone(phone);
            user.setAge(age);
            user.setGender(gender);
            String imageUrl= uploadImageService.saveImage(image);
            user.setImage(imageUrl);
            user.setPassword(password);



            userService.registerUser(user);
            return "Registered successfully. Please check your email to verify your account";

        }catch(IllegalArgumentException e){
            return "Email already exists";
        }
        catch (Exception e){
            e.printStackTrace();
            return "An unexpected error occured. Please try again later";
        }
    }
    @GetMapping("/verify")
    public void  verify(@RequestParam String code, HttpServletResponse response) throws IOException {
        boolean verified = userService.verifyUser(code);
        if(verified){
            response.sendRedirect("/login");
        }
        else{
            response.sendRedirect("/login");

        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        String response = authService.authenticate(email, password, session);

        if ("success".equals(response)) {
            String userRole = (String) session.getAttribute("userRole"); // Retrieve the role from session
            String redirectUrl = "ROLE_PATIENTS".equals(userRole) ? "/userHome" : "/home"; // Determine redirection

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("redirectUrl", redirectUrl);
            return ResponseEntity.ok(responseBody);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", response));
        }
    }

    @GetMapping("/role")
    public String getUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // Invalidates the session
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        // Check if user or staff exists
        Optional<User> userOptional = userService.findByEmail(email);
        Optional<Staff> staffOptional = staffService.findByEmail(email);

        if (userOptional.isPresent()) {
            try {
                userService.generatePasswordResetToken(userOptional.get());
                return ResponseEntity.ok("Password reset link sent to your email.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An error occurred while generating reset link. Please try again.");
            }
        } else if (staffOptional.isPresent()) {
            try {
                staffService.generatePasswordResetToken(staffOptional.get());
                return ResponseEntity.ok("Password reset link sent to your email.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An error occurred while generating reset link. Please try again.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User or staff with this email does not exist.");
        }
    }


    // Reset Password (User & Staff)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        boolean resetSuccessful = userService.resetPasswordWithToken(token, newPassword) ||
                staffService.resetPasswordWithToken(token, newPassword);

        if (resetSuccessful) {
            return ResponseEntity.ok("Password reset successful.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token or password reset failed.");
        }
    }


}

