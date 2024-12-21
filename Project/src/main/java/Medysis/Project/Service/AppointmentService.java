package Medysis.Project.Service;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment bookAppointment(Integer patientID, String doctor, LocalDate appDate, LocalTime appTime) {
        Appointment appointment = new Appointment();
        appointment.setPatientID(patientID);
        appointment.setDoctorID(doctor);
        appointment.setAppDate(appDate);
        appointment.setAppTime(appTime);
        appointment.setStatus("Pending"); // Initial status

        // Save appointment to database
        return appointmentRepository.save(appointment);
    }
}
