package Medysis.Project.Service;

import Medysis.Project.Model.Notifications;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.NotificationRepository;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StaffRepository staffRepository;

    // Methods to create notifications
    public void createUserNotifications(Integer userId, String message, String type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Notifications notification = new Notifications(user, message, type);
            notificationRepository.save(notification);
        } else {
            System.err.println("Could not create notification for User ID: " + userId + " - User not found.");
        }
    }

    public void createStaffNotifications(String staffId, String message, String type) {
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff != null) {
            Notifications notification = new Notifications(staff, message, type);
            notificationRepository.save(notification);
        } else {
            System.err.println("Could not create notification for Staff ID: " + staffId + " - Staff not found.");
        }
    }

    // Methods to get notifications
    public List<Notifications> getNotificationsForUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(user);
        }
        return List.of();
    }

    public List<Notifications> getNotificationsForStaffUser(String staffId, String role) {
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff != null) {
            if (role.equalsIgnoreCase("ROLE_DOCTOR")) {
                return notificationRepository.findByStaffIdOrderByCreatedAtDesc(staff);
            } else if (role.equalsIgnoreCase("ROLE_LAB TECHNICIAN")) {
                return notificationRepository.findByType("lab_order");
            }
        }
        return List.of();
    }

    // Methods to get unread notification count
    public long getUnreadNotificationsCountForUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return notificationRepository.countByUserIdAndStatus(user, "unread");
        }
        return 0;
    }

    public long getUnreadNotificationsCountForStaffUser(String staffId, String role) {
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff != null) {
            if (role.equalsIgnoreCase("ROLE_DOCTOR")) {
                return notificationRepository.countByStaffIdAndStatus(staff, "unread");
            } else if (role.equalsIgnoreCase("ROLE_LAB TECHNICIAN")) {
                return notificationRepository.findByType("lab_order").stream()
                        .filter(notification -> notification.getStatus().equalsIgnoreCase("unread"))
                        .count();
            }
        }
        return 0;
    }

    // Methods to mark notifications as read
    public void markNotificationsAsRead(Long notificationId) {
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notifications not found with ID: " + notificationId));
        notification.setStatus("read");
        notificationRepository.save(notification);
    }

    public void markAllNotificationsAsReadForUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Notifications> unreadNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user).stream()
                    .filter(notification -> notification.getStatus().equals("unread"))
                    .toList();
            unreadNotifications.forEach(notification -> notification.setStatus("read"));
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    public void markAllNotificationsAsReadForStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff != null) {
            List<Notifications> unreadNotifications = notificationRepository.findByStaffIdOrderByCreatedAtDesc(staff).stream()
                    .filter(notification -> notification.getStatus().equals("unread"))
                    .toList();
            unreadNotifications.forEach(notification -> notification.setStatus("read"));
            notificationRepository.saveAll(unreadNotifications);
        }
    }
}