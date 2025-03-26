package Medysis.Project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/appointment")
    public String appointment() {
        return "appointment";
    }
    @GetMapping("/addPastmedical")
    public String addPastmedical() {
        return "addPastmedical";
    }

    @GetMapping("/patientHome")
    public String patientHome() {
        return "patientHome";
    }
     @GetMapping("/forgotPassword")
    public String forgotPassword() {  return "forgotPassword"; }
    @GetMapping("/userHome")
    public String userHome() {
        return "userHome";
    }
    @GetMapping("/prescription")
    public String prescription() {
        return "prescriptionView";
    }
    @GetMapping("labView")
    public String labView() {
        return "labHistory";
    }
    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

}

