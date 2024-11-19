package Medysis.Project.Controller;

import Medysis.Project.Model.User;
import Medysis.Project.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")

public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestParam User user) {
        try{
        userService.registerUser(user);
        return ResponseEntity.ok("user registered successfully");
    }catch(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occured");
        }
    }

}
