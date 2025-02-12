package Medysis.Project.DTO;

import java.time.LocalDate;
import java.util.List;

public class LabOrderDTO {

    public int orderID;
    public int userID;
    public String userName;
    public String doctorID;
    public String doctorName;
    public int appointmentID;
    public List<LabResultDTO> labResults;
    public String urgency;
    public LocalDate orderDate;


}
