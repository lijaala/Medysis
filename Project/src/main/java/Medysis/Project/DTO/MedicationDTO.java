package Medysis.Project.DTO;

public class MedicationDTO {
    private String medicationName;
    private String dosage;
    private String intake;
    private String medicationInterval;
    private Integer daysOfIntake;

    public MedicationDTO(String medicationName, String dosage, String intake, String medicationInterval, Integer daysOfIntake) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.intake = intake;
        this.medicationInterval = medicationInterval;
        this.daysOfIntake = daysOfIntake;
    }
    // Getters and setters
}

