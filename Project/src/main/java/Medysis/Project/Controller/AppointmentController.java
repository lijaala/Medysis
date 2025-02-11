package Medysis.Project.Controller;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private final StaffRepository staffRepository;
    @Autowired
    private final AppointmentService appointmentService;

    public AppointmentController(StaffRepository staffRepository, AppointmentService appointmentService) {
        this.staffRepository = staffRepository;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/fetchDoctors")
    public List<Staff> getDoctors() {
        return staffRepository.findByRoleRoleID(2);  // Role ID for doctors
    }

    @PostMapping("/book")
    public String bookAppointment(
            @RequestParam("doctor") String doctor,
            @RequestParam("date") String appDateStr,
            @RequestParam("time") String appTimeStr,
            HttpSession session) {

        Integer patientID = (Integer) session.getAttribute("userId");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the date and time using DateTimeFormatter
        LocalDate appDate = LocalDate.parse(appDateStr, dateFormatter);
        LocalTime appTime = LocalTime.parse(appTimeStr, timeFormatter);

        // Call service to book appointment
        Appointment appointment = appointmentService.bookAppointment(patientID, doctor, appDate, appTime);

        return "Appointment added successfully";
    }

    @GetMapping("/list")
    public List<Appointment> getAppointments(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        String userId = (String) session.getAttribute("userId");


        return appointmentService.getAppointmentsByRole(userRole, userId);
    }

    @PostMapping("/edit")
    public String editAppointment(
            @RequestParam("appointmentID") Integer appointmentID,
            @RequestParam("date") String appDateStr,
            @RequestParam("time") String appTimeStr,
            @RequestParam("status") String status,
            HttpSession session) {

        // Get the doctor ID from the session (assuming it's stored as 'doctorId')
        String doctorID = (String) session.getAttribute("userId");

        // If doctorID is not found in the session, you may want to handle this error
        if (doctorID == null) {
            return "Doctor ID is not available in the session.";
        }
        System.out.println("Edit request received for appointment ID: " + appointmentID);
        try{
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the date and time
        LocalDate appDate = LocalDate.parse(appDateStr, dateFormatter);
        LocalTime appTime = LocalTime.parse(appTimeStr, timeFormatter);

        // Call service to edit the appointment
        Appointment appointment = appointmentService.editAppointment(appointmentID, doctorID, appDate, appTime, status);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "Appointment updated successfully";

    }

    @PostMapping("/complete")  // New endpoint
    public String completeAppointment(@RequestParam("appointmentID") Integer appointmentID, HttpSession session) {
        String staffId = (String) session.getAttribute("userId"); // Get staff ID from session

        if (staffId == null) {
            return "Staff ID is not available in the session.";
        }

        try {
            appointmentService.completeAppointment(appointmentID, staffId); // Call the service method
            return "Appointment status updated to Completed.";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return "Error updating appointment status: " + e.getMessage(); // Return an error message
        }
    }
}
