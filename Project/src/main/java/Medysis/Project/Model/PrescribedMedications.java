package Medysis.Project.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Prescribed_Medications")
public class PrescribedMedications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescriptionID", nullable = false)
    private Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "medicationID", nullable = false)
    private Medication medication;

    @Column(name="dosage",nullable = false)
    @JsonProperty ("dosage")
    private String dosage;

    @Column(name="intake",nullable = false)
    @JsonProperty ("intake")
    private String intake;

    @Column(name = "medicationInterval", nullable = false)
    @JsonProperty ("medicationInterval")
    private String medicationInterval;

    @Column(name="daysOfIntake",nullable = false)
    @JsonProperty ("daysOfIntake")
    private int daysOfIntake;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public String getMedicationInterval() {
        return medicationInterval;
    }

    public void setMedicationInterval(String medicationInterval) {
        this.medicationInterval = medicationInterval;
    }

    public int getDaysOfIntake() {
        return daysOfIntake;
    }

    public void setDaysOfIntake(int daysOfIntake) {
        this.daysOfIntake = daysOfIntake;
    }
    @Override
    public String toString() {
        return "PrescribedMedications{" +
                "medication=" + (medication != null ? medication.getMedicationName() : "null") +
                ", dosage='" + dosage + '\'' +
                ", intake='" + intake + '\'' +
                ", interval='" + medicationInterval + '\'' +
                ", daysOfIntake=" + daysOfIntake +
                '}';
    }

}
