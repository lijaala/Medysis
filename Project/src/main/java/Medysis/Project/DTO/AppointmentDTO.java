package Medysis.Project.DTO;

import java.time.LocalDate;
import java.time.LocalTime;



public class AppointmentDTO {
    public int appointmentID;
    public UserDTO patientID;  // Use UserDTO
    public StaffDTO doctorID; // Use StaffDTO
    public LocalDate appDate;
    public LocalTime appTime;
    public String status;
    public LocalDate followUpDate;
    public String patientName;
    // No constructors needed (usually)
}