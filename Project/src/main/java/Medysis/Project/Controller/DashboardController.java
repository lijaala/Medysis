package Medysis.Project.Controller;

import Medysis.Project.Service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
