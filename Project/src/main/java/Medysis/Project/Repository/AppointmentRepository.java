package Medysis.Project.Repository;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByDoctorID(Staff doctorID);  // Corrected to use doctorID
}
