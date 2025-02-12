package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Lab-Order")
public class LabOrder {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int orderID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User userID;

    @ManyToOne
    @JoinColumn(name = "doctorID", referencedColumnName = "staffID", nullable = false)
    private Staff doctorID;

    @OneToOne
    @JoinColumn(name = "appoinmentID", referencedColumnName = "appointmentID",  unique = true)
    private Appointment appointmentID;

    @ManyToOne
    @JoinColumn(name = "labTechnician", referencedColumnName = "staffID")
    private Staff labTechnicianID;

    @Column(name = "orderDate")
    private LocalDate orderDate;

    @Column(name = "urgency")
    private String urgency;

    @OneToMany(mappedBy = "orderID", cascade = CascadeType.ALL) // One order to many results
    private List<LabResults> labResults;

    @Column(name="labStatus", columnDefinition = "VARCHAR(255) DEFAULT 'Pending'")
    private String labStatus;


    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public User getUserID() {
        return userID;
    }

    public void setUserID(User userID) {
        this.userID = userID;
    }

    public Staff getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(Staff doctorID) {
        this.doctorID = doctorID;
    }

    public Appointment getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(Appointment appointmentID) {
        this.appointmentID = appointmentID;
    }

    public Staff getLabTechnicianID() {
        return labTechnicianID;
    }

    public void setLabTechnicianID(Staff labTechnicianID) {
        this.labTechnicianID = labTechnicianID;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public List<LabResults> getLabResults() {
        return labResults;
    }

    public void setLabResults(List<LabResults> labResults) {
        this.labResults = labResults;
    }

    public String getLabStatus() {
        return labStatus;
    }

    public void setLabStatus(String labStatus) {
        this.labStatus = labStatus;
    }
}
