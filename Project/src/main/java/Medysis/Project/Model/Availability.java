package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String availableDays;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "doctorID", referencedColumnName = "staffID")
    private Staff doctorID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Staff getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(Staff doctorID) {
        this.doctorID = doctorID;
    }
}

