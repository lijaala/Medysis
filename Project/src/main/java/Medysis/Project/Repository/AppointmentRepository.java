package Medysis.Project.Repository;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByDoctorID(Staff doctorID);

    @Query("SELECT a FROM Appointment a WHERE a.doctorID.role.role = :role")
    List<Appointment> findAppointmentsByRole(@Param("role") String role);


    // Count total appointments
    long count();

    // Count appointments by staff (for the chart data)
    long countByDoctorID(Staff staff);

    // Fetch appointment schedule data (calendar)
    @Query("SELECT a.appDate, COUNT(a), SUM(CASE WHEN a.status = 'Pending' THEN 1 ELSE 0 END) " +
            "FROM Appointment a GROUP BY a.appDate")
    List<Object[]> getCalendarStats();


    @Query("SELECT a.appDate, COUNT(a), SUM(CASE WHEN a.status = 'Pending' THEN 1 ELSE 0 END) " +
            "FROM Appointment a WHERE a.doctorID = :doctor GROUP BY a.appDate")
    List<Object[]> getDoctorCalendarStats(@Param("doctor") Staff doctor);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorID.staffID = :doctorID AND a.appDate >= CURRENT_DATE")
    long countUpcomingAppointmentsForDoctor(@Param("doctorID") String doctorID);

    long countByDoctorIDAndAppDate(Staff doctor, LocalDate date);


    List<Appointment> findByDoctorIDAndAppDate(Staff doctorID, LocalDate date);

    boolean existsByPatientIDAndDoctorIDAndAppDateAndAppTime(User patient, Staff doctor, LocalDate appDate, LocalTime appTime);

    List<Appointment> getAppointmentByPatientID(User patientID);

    List<Appointment> findByFollowUpDate(LocalDate reminderDate);
}
