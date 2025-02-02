package Medysis.Project.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "Medication")
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicationID;

    @Column(name="medicationName",nullable = false, unique = true)
    private String medicationName;

    @Column(name="alternative", nullable = true)
    private String alternative;

    // Getters and Setters

    public Long getMedicationID() {
        return medicationID;
    }

    public void setMedicationID(Long medicationID) {
        this.medicationID = medicationID;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getAlternative() {
        return alternative;
    }

    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }
}

