package Medysis.Project.Controller;

import Medysis.Project.DTO.DoctorDashboardDTO;
import Medysis.Project.Service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboardData() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalPatients", dashboardService.getTotalPatients());
        response.put("newPatients", dashboardService.getNewPatients());
        response.put("totalAppointments", dashboardService.getTotalAppointments());
        response.put("calendarData", dashboardService.getAdminCalendarData());
        response.put("chartData", dashboardService.getAppointmentsByStaff());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor")
    public ResponseEntity<DoctorDashboardDTO> getDoctorDashboardData(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");

        if (staffId == null) {
            return ResponseEntity.badRequest().build();
        }

        DoctorDashboardDTO dashboardData = dashboardService.getDoctorDashboardData(staffId);
        dashboardData.appointmentsPerDay = dashboardService.getDoctorCalendarData(staffId);

        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/lab-tech")
    public ResponseEntity<Map<String, Object>> getLabTechnicianDashboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalLabTests", dashboardService.getTotalLabTests());
        response.put("pendingLabRequests", dashboardService.getPendingLabRequests());
        response.put("urgentPendingLabRequests", dashboardService.getUrgentPendingLabRequests());

        return ResponseEntity.ok(response);
    }

}
