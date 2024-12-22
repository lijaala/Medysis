package Medysis.Project.Controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
}

