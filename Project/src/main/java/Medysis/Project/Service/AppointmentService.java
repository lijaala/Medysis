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
}
