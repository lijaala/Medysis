package Medysis.Project.Controller;

import Medysis.Project.Model.User;
import Medysis.Project.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")


public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
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
       return "success";

    }catch(IllegalArgumentException e){
        return "Email already exists";
        }
        catch (Exception e){
            return "An unexpected error occured. Please try again later";
        }
    }

}
