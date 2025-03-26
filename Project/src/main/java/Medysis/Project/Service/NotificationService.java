package Medysis.Project.Service;

import Medysis.Project.Model.Notifications;
import Medysis.Project.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    // Methods to create notifications
    public void createUserNotifications(Integer userId, String message, String type) {
        Notifications notification = new Notifications(userId, message, type);
        notificationRepository.save(notification);
    }

    public void createStaffNotifications(String staffId, String message, String type) {
        Notifications notification = new Notifications(staffId, message, type);
        notificationRepository.save(notification);
    }

    // Methods to get notifications
    public List<Notifications> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notifications> getNotificationsForStaff(String staffId) {
        return notificationRepository.findByStaffIdOrderByCreatedAtDesc(staffId);
    }

    // Methods to get unread notification count
    public long getUnreadNotificationsCountForUser(Integer userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "unread");
    }

    public long getUnreadNotificationsCountForStaff(String staffId) {
        return notificationRepository.countByStaffIdAndStatus(staffId, "unread");
    }

    // Methods to mark notifications as read
    public void markNotificationsAsRead(Integer notificationId) {
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notifications not found with ID: " + notificationId));
        notification.setStatus("read");
        notificationRepository.save(notification);
    }

    public void markAllNotificationssAsReadForUser(Integer userId) {
        List<Notifications> unreadNotificationss = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(notification -> notification.getStatus().equals("unread"))
                .toList();
        unreadNotificationss.forEach(notification -> notification.setStatus("read"));
        notificationRepository.saveAll(unreadNotificationss);
    }

    public void markAllNotificationssAsReadForStaff(String staffId) {
        List<Notifications> unreadNotificationss = notificationRepository.findByStaffIdOrderByCreatedAtDesc(staffId).stream()
                .filter(notification -> notification.getStatus().equals("unread"))
                .toList();
        unreadNotificationss.forEach(notification -> notification.setStatus("read"));
        notificationRepository.saveAll(unreadNotificationss);
    }
}
