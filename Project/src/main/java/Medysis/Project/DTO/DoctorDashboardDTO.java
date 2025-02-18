package Medysis.Project.DTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DoctorDashboardDTO {
    public long totalAppointments;
    public long appointmentsToday;    // Appointments for today
    public long totalLabReports;     // Total number of lab reports
    public Map<String, Integer> ageGroupDistribution; // Age group distribution for patients
    public Map<LocalDate, Integer> appointmentsPerDay;

}
