package Medysis.Project.Model;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="MedicalRecord")
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recordID")
    private Integer recordID;

    @Column(name = "conditionName")
    private String conditionName;

    @Column(name = "isTreated")
    private String isTreated;

    @Column(name = "scans")
    private String scans;

    @Column(name = "diagnosedDate")
    private LocalDate diagnosedDate;

    @Column(name="treatmentPlan", nullable = true)
    private String treatmentPlan;

    @ManyToOne
    @JoinColumn(name = "user", referencedColumnName = "userID")
    private User user;

    @ManyToOne
    @JoinColumn (name = "doctor", referencedColumnName="staffID", nullable= true)
    private Staff doctor;

    @ManyToOne
    @JoinColumn(name = "appointment", referencedColumnName = "appointmentID", nullable = true, unique = true)
    private Appointment appointment;  // New field linking to Appointment

    @Column(name = "alteredBy")
    private String alteredBy;
    @Column(name="deleted")
    private Boolean deleted;


    public Integer getRecordID() {
        return recordID;
    }

    public void setRecordID(Integer recordID) {
        this.recordID = recordID;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getIsTreated() {
        return isTreated;
    }

    public void setIsTreated(String isTreated) {
        this.isTreated = isTreated;
    }

    public String getScans() {
        return scans;
    }

    public void setScans(String scans) {
        this.scans = scans;
    }

    public LocalDate getDiagnosedDate() {
        return diagnosedDate;
    }

    public void setDiagnosedDate(LocalDate diagnosedDate) {
        this.diagnosedDate = diagnosedDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Staff getDoctor() {
        return doctor;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public void setDoctor(Staff doctor) {
        this.doctor = doctor;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getAlteredBy() {
        return alteredBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setAlteredBy(String alteredBy) {
        this.alteredBy = alteredBy;
    }
}

