package Medysis.Project.Repository;

import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.LabOrder;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Integer> {


    @Query("SELECT l.orderDate AS date, COUNT(l) AS totalReports, " +
            "SUM(CASE WHEN l.urgency = 'yes' THEN 1 ELSE 0 END) AS urgentReports " +
            "FROM LabOrder l GROUP BY l.orderDate")
    List<Object[]> getLabScheduleStats();

    long countByDoctorID(Staff doctor);

    long countByLabStatus(String status);
    long countByLabStatusAndUrgency(String status, String urgency);

    List<LabOrder> findByLabStatusAndUrgency(@Param("labStatus") String labStatus, @Param("urgency") String urgency);


    List<LabOrder> findByUserID(User userID);

    List<LabOrder> findByAppointmentID(Appointment appointmentID); // Add this method

}
