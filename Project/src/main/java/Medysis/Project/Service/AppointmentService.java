package Medysis.Project.Service;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.AppointmentRepository;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;

    public Appointment bookAppointment(Integer patientID, String doctorID, LocalDate appDate, LocalTime appTime) {
        User patient = userRepository.findById(patientID)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff doctor = staffRepository.findById(doctorID)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setPatientID(patient);
        appointment.setDoctorID(doctor);
        appointment.setAppDate(appDate);
        appointment.setAppTime(appTime);
        appointment.setStatus("Pending"); // Initial status

        // Save appointment to database
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByRole(String userRole, String userId) {


        if ("ROLE_DOCTOR".equals(userRole)) {
            Staff doctor = staffRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            return appointmentRepository.findByDoctorID(doctor);
        } else {

            return appointmentRepository.findAll();
            // Empty list for non-admin/non-doctor roles
        }
    }

    public Appointment editAppointment(Integer appointmentID, String doctorID, LocalDate appDate, LocalTime appTime, String status) {
        // Fetch the appointment from the database
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Update only the fields that are provided (not empty)
        if (doctorID != null && !doctorID.isEmpty()) {
            Staff doctor = staffRepository.findById(doctorID)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            appointment.setDoctorID(doctor);
        }

        if (appDate != null) {
            appointment.setAppDate(appDate);
        }

        if (appTime != null) {
            appointment.setAppTime(appTime);
        }

        if (status != null && !status.isEmpty()) {
            appointment.setStatus(status);
        }

        // Save the updated appointment
        return appointmentRepository.save(appointment);
    }
}
