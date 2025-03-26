package Medysis.Project.Controller;


import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.DTO.LabResultDTO;

import Medysis.Project.Model.User;
import Medysis.Project.Repository.UserRepository;
import Medysis.Project.Service.LabOrderService;
import Medysis.Project.Service.LabResultService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/LabOrder")
public class LabOrderController {

    @Autowired
    private LabOrderService labOrderService;

    @Autowired
    private LabResultService labResultService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/orderRequest")
    public ResponseEntity<String> createOrUpdateLabOrder(@RequestParam("appointmentID") int appointmentId,
                                                         @RequestParam("userID") int userId,
                                                         @RequestParam("urgency") String urgency,
                                                         @RequestParam("testName[]") List<Integer> testIds,
                                                         @RequestParam(value = "orderID", required = false) Integer orderId,
                                                         HttpSession session) {

        String staffId = (String) session.getAttribute("userId");

        try {
            if (orderId != null) {
                labOrderService.updateLabOrder(orderId, appointmentId, userId, staffId, urgency, testIds);
                return ResponseEntity.ok("Lab Order updated successfully");
            } else {
                labOrderService.createLabOrder(appointmentId, userId, staffId, urgency, testIds);
                return ResponseEntity.status(HttpStatus.CREATED).body("Lab Order created successfully");
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create/update lab order: " + e.getMessage());
        }
    }


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

    @GetMapping("/getByUserId")
    public ResponseEntity<List<LabOrderDTO>> getLabOrdersByUserId(@RequestParam (value = "userId", required = false) Integer userId, HttpSession session) {
        if (userId == null) {
            String userIdStr = (String) session.getAttribute("userId");
            if (userIdStr != null) {
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(null); // Or handle the error appropriately
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }

        final Integer finalUserId = userId;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + finalUserId));

        List<LabOrderDTO> labOrders = labOrderService.getLabOrdersByUserId(user);
        return ResponseEntity.ok(labOrders);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<LabOrderDTO>> getLabOrdersByAppointmentId(@PathVariable int appointmentId) {
        List<LabOrderDTO> labOrders = labOrderService.getLabOrdersByAppointmentId(appointmentId);
        return ResponseEntity.ok(labOrders);
    }
}



