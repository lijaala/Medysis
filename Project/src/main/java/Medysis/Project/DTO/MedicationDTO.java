package Medysis.Project.DTO;

public class MedicationDTO {
    private String medicationName;
    private String dosage;
    private String intake;
    private String medicationInterval;
    private Integer daysOfIntake;

    public MedicationDTO() {}

    public MedicationDTO(String medicationName, String dosage, String intake, String medicationInterval, Integer daysOfIntake) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.intake = intake;
        this.medicationInterval = medicationInterval;
        this.daysOfIntake = daysOfIntake;
    }
    // Getters and setters

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
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

    public Integer getDaysOfIntake() {
        return daysOfIntake;
    }

    public void setDaysOfIntake(Integer daysOfIntake) {
        this.daysOfIntake = daysOfIntake;
    }
}

