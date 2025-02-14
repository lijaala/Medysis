package Medysis.Project.Controller;

import Medysis.Project.Service.LabResultService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("labResult/")
public class LabResultController {
    @Autowired
    LabResultService labResultService;

    @PutMapping("/update/{orderId}/{testId}")
    public ResponseEntity<String> updateLabResult(
            @PathVariable int orderId,
            @PathVariable int testId,
            @RequestParam Double resultValue,
            @RequestParam(required = false) String notes,
            HttpSession session) {

        try {
            labResultService.updateLabResult(orderId, testId, resultValue, notes, session);
            return ResponseEntity.ok("Lab result updated successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception details
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating lab result.");
        }
    }
}
