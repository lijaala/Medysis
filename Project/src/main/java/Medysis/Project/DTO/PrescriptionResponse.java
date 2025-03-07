package Medysis.Project.DTO;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionResponse {
    private Integer userId;
    private String prescribedBy;
    private LocalDate prescriptionDate;

    private List<MedicationDTO> medications;

    public PrescriptionResponse(Integer userId, String prescribedBy, LocalDate prescriptionDate,  List<MedicationDTO> medications) {
        this.userId = userId;
        this.prescribedBy = prescribedBy;
        this.prescriptionDate = prescriptionDate;

        this.medications = medications;
    }
    // Getters and setters
}
