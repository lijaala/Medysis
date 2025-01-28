package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointmentID")
    private int appointmentID;

    @ManyToOne
    @JoinColumn(name = "patientID", referencedColumnName = "userID", nullable = false)
     private User patientID;

    @ManyToOne
    @JoinColumn(name = "doctorID", referencedColumnName = "staffID", nullable = false)

    private Staff doctorID;

    @Column(name ="appDate")
    private LocalDate appDate;

    @Column(name ="appTime")
    private LocalTime appTime;

    @Column(name = "status")
    private String status;

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public User getPatientID() {
        return patientID;
    }

    public void setPatientID(User patientID) {
        this.patientID = patientID;
    }

    public Staff getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(Staff doctorID) {
        this.doctorID = doctorID;
    }

    public LocalDate getAppDate() {
        return appDate;
    }

    public void setAppDate(LocalDate appDate) {
        this.appDate = appDate;
    }

    public LocalTime getAppTime() {
        return appTime;
    }

    public void setAppTime(LocalTime appTime) {
        this.appTime = appTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

