package Medysis.Project.Service;

import Medysis.Project.DTO.AppointmentDTO;
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
import java.util.Optional;

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



    public Appointment editAppointment(Integer appointmentID, String updatedBy, LocalDate appDate, LocalTime appTime, String status) {
        // Fetch the appointment from the database
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Update only the fields that are provided (not empty)

        if (appDate != null) {
            appointment.setAppDate(appDate);
        }

        if (appTime != null) {
            appointment.setAppTime(appTime);
        }

        if (status != null && !status.isEmpty()) {
            appointment.setStatus(status);
        }
        appointment.setAppUpdatedBy(updatedBy);
        // Save the updated appointment
        return appointmentRepository.save(appointment);
    }



    public void completeAppointment(Integer appointmentID, String staffId) {
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found for ID: " + appointmentID));

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

        if (!appointment.getDoctorID().getStaffID().equals(staff.getStaffID())) {
            throw new RuntimeException("You are not authorized to complete this appointment.");
        }

        appointment.setStatus("Completed"); // Update the status
        appointmentRepository.save(appointment); // Save the changes
    }
    public boolean appointmentExists(Integer patientID, String doctorID, LocalDate appDate, LocalTime appTime) {
        User patient = userRepository.findById(patientID)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff doctor = staffRepository.findById(doctorID)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return appointmentRepository.existsByPatientIDAndDoctorIDAndAppDateAndAppTime(patient, doctor, appDate, appTime);
    }



     public List<Appointment> getAllAppointments(){
        return appointmentRepository.findAll();
     }

    public List<Appointment> getAppointmentsByDoctorAndDate(String doctorID, LocalDate date) {
        // Fetch the Staff entity from the repository
        Optional<Staff> staffOpt = staffRepository.findById(doctorID);

        if (staffOpt.isPresent()) {
            Staff doctor = staffOpt.get();
            return appointmentRepository.findByDoctorIDAndAppDate(doctor, date);
        } else {
            throw new RuntimeException("Doctor with ID " + doctorID + " not found.");
        }
    }
    public List<Appointment> getAppointmentByUserId(Integer userID) {
        User user=userRepository.findById(userID).orElseThrow(() -> new RuntimeException("Patient not found"));

        return appointmentRepository.getAppointmentByPatientID(user);

    }

}
