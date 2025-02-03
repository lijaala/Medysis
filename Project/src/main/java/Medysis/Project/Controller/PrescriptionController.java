package Medysis.Project.Controller;

import Medysis.Project.Model.Prescription;
import Medysis.Project.Service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    @Autowired
    private PrescriptionService prescriptionService;

    // Endpoint to add a new prescription
    @PostMapping("/add/{appointmentId}")
    public ResponseEntity<Object> addPrescription(
            @PathVariable("appointmentId") Integer appointmentId,
            @RequestBody Prescription prescription, // Prescription received from frontend
            HttpSession session) {

        // Log received prescription for debugging
        logger.info("Received Prescription: " + prescription);

        // Fetch staffId from session
        String staffId = (String) session.getAttribute("userId");

        // Fetch userId from the prescription (coming from the frontend)
        Integer userId = prescription.getUser().getUserID(); // Assuming the user object is part of the prescription

        // Check if prescription is null and userId or staffId are missing
        if (prescription == null) {
            logger.error("Prescription cannot be null");
            return ResponseEntity.status(400).body(new ErrorResponse("Prescription cannot be null"));
        }

        if (userId == null) {
            logger.error("User cannot be null");
            return ResponseEntity.status(400).body(new ErrorResponse("User cannot be null"));
        }

        if (staffId == null) {
            logger.error("Staff cannot be null");
            return ResponseEntity.status(400).body(new ErrorResponse("Staff cannot be null"));
        }

        if (prescription.getAppointment() == null) {
            logger.error("Appointment cannot be null");
            return ResponseEntity.status(400).body(new ErrorResponse("Appointment cannot be null"));
        }

        try {
            // Call the service layer to process the prescription
            prescriptionService.addPrescription(appointmentId, prescription, staffId, userId);
            logger.info("Prescription added successfully");
            return ResponseEntity.ok().body("Prescription added successfully");
        } catch (Exception e) {
            logger.error("Error while adding prescription: ", e);
            return ResponseEntity.status(500).body(new ErrorResponse("Error while adding prescription: " + e.getMessage()));
        }
    }

    // Error response class to send JSON formatted error messages
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
