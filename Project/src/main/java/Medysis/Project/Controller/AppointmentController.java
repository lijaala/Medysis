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

    @PostMapping ("/fetchDoctors")
    public List<Staff> getDoctors(){
        return staffRepository.findByRoleRoleID(2);
    }
    @PostMapping("/book")
    public Appointment bookAppointment(
            @RequestParam("doctor") String doctor,
            @RequestParam("date") String appDateStr,
            @RequestParam("time") String appTimeStr,
            HttpSession session) {

        // Define the format for the date and time strings
        Integer patientID = (Integer) session.getAttribute("userId");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Parse the date and time using DateTimeFormatter
        LocalDate appDate = LocalDate.parse(appDateStr, dateFormatter);
        LocalTime appTime = LocalTime.parse(appTimeStr, timeFormatter);

        // Call service to book appointment
        Appointment appointment = appointmentService.bookAppointment(patientID, doctor, appDate, appTime);

        return appointment; // Return the appointment object
    }

}