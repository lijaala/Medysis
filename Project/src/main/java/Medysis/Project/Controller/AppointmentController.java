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
        Object userIdObj = session.getAttribute("userId");
        String userId = (userIdObj instanceof String) ? (String) userIdObj : String.valueOf(userIdObj);


        return appointmentService.getAppointmentsByRole(userRole, userId);
    }

    @PostMapping("/edit")
    public String editAppointment(
            @RequestParam("appointmentID") Integer appointmentID,
            @RequestParam("date") String appDateStr,
            @RequestParam("time") String appTimeStr,
            @RequestParam("status") String status,
            HttpSession session) {

        // Get the doctor ID from the session
        String doctorID = (String) session.getAttribute("userId");

        // If doctorID is not found in the session, you may want to handle this error
        if (doctorID == null) {
            return "Doctor ID is not available in the session.";
        }
        System.out.println("Edit request received for appointment ID: " + appointmentID);
        try{
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss]");


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



        // Fetch the doctor's availability from the repository
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

        // Generate time slots (e.g., every 30 minutes)
        List<String> timeSlots = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalTime slot = startTime;
        while (slot.isBefore(endTime)) {
            timeSlots.add(slot.format(timeFormatter));
            slot = slot.plusMinutes(8); // Adjust slot duration as needed
        }

        // Fetch existing appointments for the doctor on the given date
        List<Appointment> bookedAppointments = appointmentService.getAppointmentsByDoctorAndDate(doctorID, date);

        // Remove already booked slots
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

}
