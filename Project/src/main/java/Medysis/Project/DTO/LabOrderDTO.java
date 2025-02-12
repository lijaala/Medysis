package Medysis.Project.DTO;

import java.time.LocalDate;
import java.util.List;

public class LabOrderDTO {

    public int orderID;
    public UserDTO userID;
    public StaffDTO doctorID;
    public AppointmentDTO appointmentID;
    public List<LabResultDTO> labResults;
    public String urgency;
    public LocalDate orderDate;

}
