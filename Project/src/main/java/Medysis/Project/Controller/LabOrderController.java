package Medysis.Project.Controller;


import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.DTO.LabResultDTO;
import Medysis.Project.Model.LabOrder;
import Medysis.Project.Service.LabOrderService;
import Medysis.Project.Service.LabResultService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/LabOrder")
public class LabOrderController {

    @Autowired
    private LabOrderService labOrderService;

    @Autowired
    private LabResultService labResultService;

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

//    @GetMapping("/allOrders")
//    public List<LabOrder> getAllLabOrders() {
//        return labOrderService.getAllLabOrders(); // Call the service
//    }

    @GetMapping("/details/{orderID}")
    public ResponseEntity<LabOrderDTO> getLabOrderDetails(@PathVariable int orderID) {
        LabOrderDTO labOrderDTO = labOrderService.getLabOrderDetails(orderID);
        if (labOrderDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(labOrderDTO);
    }

    @GetMapping("/all")
    public List<LabOrderDTO> getAllLabOrders() {
        return labOrderService.getAllLabOrders();
    }

    @GetMapping("/labResults")
    public List<LabResultDTO> getAllLabResults() { // Returns List<LabResultsDTO>
        return labResultService.getAllLabResults();
    }



}
