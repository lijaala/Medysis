package Medysis.Project.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "Lab-Results")

public class LabResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reportId;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User userID;

    @ManyToOne
    @JoinColumn(name = "doctorID", referencedColumnName = "staffID", nullable = false)
    private Staff doctorID;

    @ManyToOne
    @JoinColumn(name = "appoinmentID", referencedColumnName = "appointmentID", nullable = true)
    private Appointment appointmentID;

    @ManyToOne
    @JoinColumn(name = "orderID", referencedColumnName = "orderID")
    private LabOrder orderID;

    @ManyToOne
    @JoinColumn(name = "testID", referencedColumnName = "testID")
    private LabTests testID;

    @Column(name = "resultValue", nullable = true)
    private Integer resultValue;

    @Column(name = "notes", nullable = true)
    private String notes;

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
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

    public LabOrder getOrderID() {
        return orderID;
    }

    public void setOrderID(LabOrder orderID) {
        this.orderID = orderID;
    }

    public LabTests getTestID() {
        return testID;
    }

    public void setTestID(LabTests testID) {
        this.testID = testID;
    }
}
