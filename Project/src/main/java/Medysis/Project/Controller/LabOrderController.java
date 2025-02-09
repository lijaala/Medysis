package Medysis.Project.Controller;


import Medysis.Project.Service.LabOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/LabOrder")
public class LabOrderController {

    @Autowired
    private LabOrderService labOrderService;

    @PostMapping("/orderRequest")
    public String createLabOrder(@RequestParam("appointmentID") int appointmentId,
                                 @RequestParam("userID") int userId,

                                 @RequestParam("urgency") String urgency,
                                 @RequestParam("testName[]") List<Integer> testIds,
                                 HttpSession session) {

        String staffId = (String) session.getAttribute("userId");


        labOrderService.createLabOrder(appointmentId, userId,staffId, urgency, testIds);
        return "redirect:/success"; // Or redirect as needed
    }

}
