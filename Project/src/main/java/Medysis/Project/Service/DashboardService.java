package Medysis.Project.Service;

import Medysis.Project.DTO.AppointmentDTO;
import Medysis.Project.DTO.DoctorDashboardDTO;
import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.LabOrder;
import Medysis.Project.Model.LabResults;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private LabOrderService labOrderService;
    @Autowired
    private LabTestRepository labTestRepository;

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

    //for doctor

    public DoctorDashboardDTO getDoctorDashboardData(String doctorID) {
        DoctorDashboardDTO dashboard = new DoctorDashboardDTO();


        Optional<Staff> staffOptional = staffRepository.findById(doctorID);
        if (staffOptional.isEmpty()) {
            return dashboard; // Return empty dashboard if staff not found
        }
        Staff staff = staffOptional.get();

        // Get total appointments for the doctor
        dashboard.totalAppointments = appointmentRepository.countByDoctorID(staff);

        // Get upcoming appointments
        LocalDate today = LocalDate.now();
        dashboard.appointmentsToday = appointmentRepository.countByDoctorIDAndAppDate(staff, today);

        // Get the number of lab reports (total count)
        dashboard.totalLabReports = labOrderRepository.countByDoctorID(staff);

        dashboard.ageGroupDistribution = getAgeGroupDistribution(staff);


        return dashboard;
    }
    private Map<String, Integer> getAgeGroupDistribution(Staff doctor) {
        // Define age groups
        Map<String, Integer> ageGroups = new HashMap<>();
        ageGroups.put("0-18", 0);
        ageGroups.put("19-35", 0);
        ageGroups.put("36-50", 0);
        ageGroups.put("51-65", 0);
        ageGroups.put("66+", 0);

        // Get all patients associated with the doctor
        List<Appointment> appointments = appointmentRepository.findByDoctorID(doctor);

        for (Appointment appointment : appointments) {
            // Fetch the patient's age
            int age = appointment.getPatientID().getAge(); // Adjust this based on your actual structure
            if (age >= 0 && age <= 18) {
                ageGroups.put("0-18", ageGroups.get("0-18") + 1);
            } else if (age >= 19 && age <= 35) {
                ageGroups.put("19-35", ageGroups.get("19-35") + 1);
            } else if (age >= 36 && age <= 50) {
                ageGroups.put("36-50", ageGroups.get("36-50") + 1);
            } else if (age >= 51 && age <= 65) {
                ageGroups.put("51-65", ageGroups.get("51-65") + 1);
            } else {
                ageGroups.put("66+", ageGroups.get("66+") + 1);
            }
        }

        return ageGroups;
    }
    public Map<LocalDate, Integer> getDoctorCalendarData(String doctorID) {
        Map<LocalDate, Integer> calendarData = new HashMap<>();

        // Fetch the doctor based on the doctorID
        Optional<Staff> staffOptional = staffRepository.findById(doctorID);
        if (staffOptional.isEmpty()) {
            return calendarData; // Return empty map if staff not found
        }
        Staff staff = staffOptional.get();

        // Fetch the appointments for the doctor using the custom query in the repository
        List<Object[]> results = appointmentRepository.getDoctorCalendarStats(staff);

        // Map the results to a more usable format (LocalDate -> appointment count)
        for (Object[] result : results) {
            LocalDate appDate = (LocalDate) result[0]; // Assuming the date is at index 0
            Integer count = ((Long) result[1]).intValue(); // Count is at index 1
            calendarData.put(appDate, count);
        }

        return calendarData;
    }

    //for lab tech
    public long getTotalLabTests() {
        return labTestRepository.count(); // Count total lab tests
    }

    public long getPendingLabRequests() {
        return labOrderRepository.countByLabStatus("Pending"); // Assuming 'Pending' is the status for pending requests
    }

    public long getUrgentPendingLabRequests() {
        return labOrderRepository.countByLabStatusAndUrgency("Pending", "High"); // Fetch urgent pending lab requests
    }




}



