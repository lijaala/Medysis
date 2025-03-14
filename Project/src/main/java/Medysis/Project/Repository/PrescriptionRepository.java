package Medysis.Project.Repository;

import Medysis.Project.Model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    Optional<Prescription> findByAppointment_AppointmentID(Integer appointmentId);

    List<Prescription> findByUserId(Integer userId);
    Optional<Prescription> findByAppointmentAppointmentID(Integer appointmentId);
}

