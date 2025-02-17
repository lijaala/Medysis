package Medysis.Project.Service;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.AppointmentRepository;
import Medysis.Project.Repository.LabOrderRepository;
import Medysis.Project.Repository.UserRepository;
import Medysis.Project.Repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private LabOrderRepository labOrderRepository;  // Add the LabReportRepository
    @Autowired
    private StaffRepository staffRepository;  // Add the StaffRepository

    public int getTotalPatients() {
        return (int) userRepository.count();  // Assuming all users are patients
    }

    public int getNewPatients() {
        return userRepository.countNewPatientsLastMonth(java.time.LocalDateTime.now().minusMonths(1));
    }
    
    public List<Appointment> getAppointmentsByRole(String role) {
        return appointmentRepository.findAppointmentsByRole(role);
    }

    public long getTotalAppointments() {
        return appointmentRepository.count();
    }

    // Fetch appointment stats for admin dashboard (calendar stats)
    public List<Object[]> getAdminCalendarData() {
        return appointmentRepository.getCalendarStats();
    }

    // Fetch lab schedule stats for admin (including urgent cases)
    public List<Object[]> getLabScheduleStats() {
        return labOrderRepository.getLabScheduleStats();  // Adjusted method to work with the query in LabReportRepository
    }

    // Get the number of appointments for each staff (for chart data)
    public Map<String, Integer> getAppointmentsByStaff() {
        Map<String, Integer> chartData = new HashMap<>();
        List<Staff> staffMembers = staffRepository.findAll();
        for (Staff staff : staffMembers) {
            long count = appointmentRepository.countByDoctorID(staff);
            chartData.put(staff.getStaffName(), (int) count);  // Get the count of appointments for each staff member
        }
        return chartData;
    }
}
