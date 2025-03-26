package Medysis.Project.Repository;

import Medysis.Project.Model.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Integer> {
    List<Notifications> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notifications> findByStaffIdOrderByCreatedAtDesc(String staffId);
    long countByUserIdAndStatus(Integer userId, String status);
    long countByStaffIdAndStatus(String staffId, String status);
}
