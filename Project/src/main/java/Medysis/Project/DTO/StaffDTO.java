package Medysis.Project.DTO;

import Medysis.Project.DTO.RoleDTO;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class StaffDTO {
    public String staffID;
    public String staffName;
    public String staffEmail;
    public String staffPhone;
    public String staffAddress;
    public String gender;
    public Integer age;
    public String image;
    public RoleDTO role; // Use RoleDTO
    public LocalDateTime addedOn;
    public LocalDateTime lastActive;
    public LocalDateTime lastUpdated;
    public LocalTime startTime;
    public LocalTime endTime;

    // No constructors needed (usually)
}