package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Prescription")
public class Prescription {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long prescriptionID;

        @ManyToOne
        @JoinColumn(name = "userID", nullable = false)
        private User user;

        @ManyToOne
        @JoinColumn(name = "staffID", nullable = false)
        private Staff staff;

        @OneToOne
        @JoinColumn(name = "appointmentID", nullable = false)
        private Appointment appointment;

        @Column(nullable = false)
        private LocalDate prescriptionDate = LocalDate.now();

        // Getters and Setters

    public Long getPrescriptionID() {
        return prescriptionID;
    }

    public void setPrescriptionID(Long prescriptionID) {
        this.prescriptionID = prescriptionID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }
}


