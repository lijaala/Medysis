package Medysis.Project.Controller;

import Medysis.Project.DTO.PrescriptionResponse;
import Medysis.Project.Model.Prescription;
import Medysis.Project.Service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


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

            @RequestBody Prescription prescription,
            HttpSession session) {


        logger.info("Received Prescription: " + prescription);

        if (prescription.getPrescribedMedications() == null || prescription.getPrescribedMedications().isEmpty()) {
            return ResponseEntity.badRequest().body("No prescribed medications found.");
        }
        System.out.println(prescription.getPrescribedMedications());

        Integer userId=prescription.getUser().getUserID();
        String staffId = (String) session.getAttribute("userId");
        System.out.println(staffId);

        if (userId == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("User cannot be null"));
        }


        if (prescription.getUser() == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("User cannot be null"));
        }
        if (staffId == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("Staff cannot be null"));
        }
        if (prescription.getAppointment() == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("Appointment cannot be null"));
        }


        try {
            prescriptionService.addPrescription(appointmentId, prescription, staffId, userId);
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



        @GetMapping("/getByUserId")
        public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByUserId(@RequestParam Integer userId) {
            List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptionsByUserId(userId);
            return ResponseEntity.ok(prescriptions);
        }
    }


