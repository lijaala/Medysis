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
import java.util.Optional;
import java.util.UUID;

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
    public void login(@RequestParam(required = true) String email, @RequestParam(required = true) String password, HttpSession session, HttpServletResponse response) throws IOException {
        Optional<User> userOptional = userService.findByEmail(email);
        Optional<Staff> staffOptional = staffService.findByEmail(email);

        if (userOptional.isPresent()) {
            authenticateUser(userOptional.get(), password, session, response, "/home");
            return;
        } else if (staffOptional.isPresent()) {
            autheticateStaff(staffOptional.get(), password, session, response, "/home");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        }
    }

    private void authenticateUser(User user, String password, HttpSession session, HttpServletResponse response, String redirectUrl) throws IOException {
        if (!user.isVerified()){
            emailService.sendVerificationEmail(user);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not verified. Verification email sent.");
            return ;
        }
        if (!passwordEncoder.matches(password, user.getPassword())){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());
        response.sendRedirect(redirectUrl);
    }
    private void autheticateStaff(Staff staff, String password, HttpSession session, HttpServletResponse response, String redirectUrl) throws IOException {
        if (!passwordEncoder.matches(password, staff.getPassword())){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }
        String roleName="ROLE_"+roleService.getRoleNameById(staff.getRole().getRoleID());
        session.setAttribute("userId", staff.getStaffID());
        session.setAttribute("userEmail",staff.getStaffEmail());
        session.setAttribute("userRole", roleName);
        response.sendRedirect(redirectUrl);
    }







}


