package Medysis.Project.Controller;

import Medysis.Project.Model.User;
import Medysis.Project.Service.EmailService;
import Medysis.Project.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")


public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name, @RequestParam String password, @RequestParam String email) {
        System.out.println("Received signup request with name: " + name + ", email: " + email + ", password: " + password);

        try{
            User user=new User();
       user.setName(name);
       user.setEmail(email);
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
    public void login(@RequestParam(required = true) String email, @RequestParam(required = true) String password, HttpSession session, HttpServletResponse response) throws IOException{
        Optional<User> userOptional=userService.findByEmail(email);
        if (!userOptional.isPresent()){
            return;
        }
        User user=userOptional.get();


        if (!user.isVerified()){
            emailService.sendVerificationEmail(user);
            return ;
        }
        if (!passwordEncoder.matches(password, user.getPassword())){
            return ;
        }
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());
        response.sendRedirect("/home");

    }

}
