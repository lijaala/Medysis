package Medysis.Project.Controller;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

        String userId = (String) session.getAttribute("userId");
        Integer patientID = Integer.parseInt(userId);

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
        Object userIdObj = session.getAttribute("userId");
        String userId = (userIdObj instanceof String) ? (String) userIdObj : String.valueOf(userIdObj);


        return appointmentService.getAppointmentsByRole(userRole, userId);
    }

    @PostMapping("/edit")
    public String editAppointment(
            @RequestParam("appointmentID") Integer appointmentID,
            @RequestParam(value = "date", required = false) String appDateStr,
            @RequestParam(value = "time", required = false) String appTimeStr,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session) {

        // Get the user ID from the session
        String updatedBy = (String) session.getAttribute("userId");

        if (updatedBy == null) {
            return "Error: User ID not found in session.";
        }
        System.out.println("Edit request received for appointment ID: " + appointmentID);

        LocalDate appDate = null;
        LocalTime appTime = null;

        if (appDateStr != null && !appDateStr.isEmpty()) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                appDate = LocalDate.parse(appDateStr, dateFormatter);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle invalid date format error, maybe return an error message to the user
            }
        }

        if (appTimeStr != null && !appTimeStr.isEmpty()) {
            try {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                appTime = LocalTime.parse(appTimeStr, timeFormatter);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle invalid time format error, maybe return an error message to the user
            }
        }

        try {
            // Call service to edit the appointment
            appointmentService.editAppointment(appointmentID, updatedBy, appDate, appTime, status);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle other potential errors during appointment update
            return "Error updating appointment.";
        }
        return "Appointment updated successfully";
    }

    @PostMapping("/complete")
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

    @GetMapping("/appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }


    @GetMapping("/availableSlots")
    public List<String> getAvailableSlots(@RequestParam("doctorID") String doctorID,
                                          @RequestParam("date") String dateStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);

        Optional<Staff> doctorOpt = staffRepository.findById(doctorID);
        if (doctorOpt.isEmpty()) {
            System.out.println("Doctor not found in database: " + doctorID);
            return List.of("Doctor not found");
        }
        Staff doctor = doctorOpt.get();
        LocalTime startTime = doctor.getStartTime();
        LocalTime endTime = doctor.getEndTime();

        if (startTime == null || endTime == null) {
            return List.of("Doctor's availability not set");
        }
        List<String> timeSlots = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime slot = startTime;
        while (slot.isBefore(endTime)) {
            timeSlots.add(slot.format(timeFormatter));
            slot = slot.plusMinutes(8); // Adjust slot duration as needed
        }

        List<Appointment> bookedAppointments = appointmentService.getAppointmentsByDoctorAndDate(doctorID, date);

        timeSlots.removeIf(slotTime -> bookedAppointments.stream()
                .anyMatch(appt -> appt.getAppTime().format(timeFormatter).equals(slotTime)));

        return timeSlots;
    }


    @PostMapping("/admin/book")
    public String bookAppointmentForPatient(
            @RequestParam("patientID") Integer patientID,  // Admin will provide patientID
            @RequestParam("doctor") String doctor,
            @RequestParam("date") String appDateStr,
            @RequestParam("time") String appTimeStr) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH); // 12-hour format with AM/PM


        // Parse date & time
        LocalDate appDate = LocalDate.parse(appDateStr, dateFormatter);
        LocalTime appTime = LocalTime.parse(appTimeStr, timeFormatter);
        if (appointmentService.appointmentExists(patientID, doctor, appDate, appTime)) {
            return "Appointment already exists."; // Return error message
        }

        // Call service to book appointment
        Appointment appointment = appointmentService.bookAppointment(patientID, doctor, appDate, appTime);

        return "Appointment added successfully by Admin.";
    }
    @GetMapping("/getByUserID")
    public List<Appointment> getAppointmentsByUerId(HttpSession session) {
        String user= (String) session.getAttribute("userId");
        Integer patientID=Integer.parseInt(user);
        return appointmentService.getAppointmentByUserId(patientID);
    }
    @PostMapping("/cancel/{appointmentID}")
    public ResponseEntity<String> cancelAppointment(@PathVariable Integer appointmentID, HttpSession session) {

        try {
            String userID= (String)session.getAttribute("userId");
            appointmentService.cancelAppointment(appointmentID, userID);
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to cancel appointment.");
        }
    }

}
