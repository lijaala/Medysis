package Medysis.Project.Controller;

import Medysis.Project.Model.PrescribedMedications;
import Medysis.Project.Model.Prescription;
import Medysis.Project.Service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ✅ Constructor Injection
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/add/{appointmentId}")
    public ResponseEntity<Prescription> addPrescription(
            @PathVariable Integer appointmentId,
            @RequestBody List<PrescribedMedications> prescribedMedications) {
        Prescription prescription = prescriptionService.addPrescription(appointmentId, prescribedMedications);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }
}
