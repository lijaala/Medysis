package Medysis.Project.Repository;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordsRepository  extends JpaRepository<MedicalRecord, Integer> {

    List<MedicalRecord> findByUserId(Integer userId);

    MedicalRecord findByAppointment(Appointment appointmentId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.user.userID = :userId AND mr.deleted = false")
    List<MedicalRecord> findByUserIdAndDeletedFalse(@Param("userId") Integer userId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.appointment = :appointment AND mr.deleted = false")
    MedicalRecord findByAppointmentAndDeletedFalse(@Param("appointment") Appointment appointment);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.deleted = false")
    List<MedicalRecord> findAllByDeletedFalse();

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.recordID = :recordId AND mr.deleted = false")
    Optional<MedicalRecord> findByIdAndDeletedFalse(@Param("recordId") Integer recordId);

}
