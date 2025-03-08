package Medysis.Project.DTO;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionResponse {
    private String userId;
    private String prescribedBy;
    private LocalDate prescriptionDate;

    private List<MedicationDTO> medications;

    public PrescriptionResponse() {}
    public PrescriptionResponse(String userId, String prescribedBy, LocalDate prescriptionDate,  List<MedicationDTO> medications) {
        this.userId = userId;
        this.prescribedBy = prescribedBy;
        this.prescriptionDate = prescriptionDate;

        this.medications = medications;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPrescribedBy() {
        return prescribedBy;
    }

    public void setPrescribedBy(String prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public List<MedicationDTO> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationDTO> medications) {
        this.medications = medications;
    }
}
