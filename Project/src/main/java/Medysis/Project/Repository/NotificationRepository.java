package Medysis.Project.Repository;

import Medysis.Project.Model.Notifications;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    List<Notifications> findByUserIdOrderByCreatedAtDesc(User user);
    List<Notifications> findByStaffIdOrderByCreatedAtDesc(Staff staff);
    long countByUserIdAndStatus(User user, String status);
    long countByStaffIdAndStatus(Staff staff, String status);
    List<Notifications> findByType(String type); // For lab technician

}