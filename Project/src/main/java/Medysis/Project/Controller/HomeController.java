package Medysis.Project.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/notification")
    public String notification(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        String userId = null;
        if (userIdObj != null) {
            userId = String.valueOf(userIdObj);
        }
        String userRole = (String) session.getAttribute("userRole");

        model.addAttribute("userId", userId); // Make userId available to the template
        model.addAttribute("userRole", userRole); // Make userRole available
        return "notification";
    }


}

