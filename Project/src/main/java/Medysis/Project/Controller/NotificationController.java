package Medysis.Project.Controller;

import Medysis.Project.Model.Notifications;
import Medysis.Project.Service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @GetMapping("/user")
    public ResponseEntity<List<Notifications>> getUserNotifications(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof String) {
            try {
                int userId = Integer.parseInt((String) userIdObj);
                List<Notifications> notifications = notificationService.getNotificationsForUser(userId);
                return ResponseEntity.ok(notifications);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(List.of()); // Or a more specific error
            }
        } else if (userIdObj instanceof Integer) {
            List<Notifications> notifications = notificationService.getNotificationsForUser((Integer) userIdObj);
            return ResponseEntity.ok(notifications);
        }
        return ResponseEntity.status(401).body(List.of()); // Unauthorized if no user ID
    }

    @GetMapping("/user/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof String) {
            try {
                int userId = Integer.parseInt((String) userIdObj);
                long count = notificationService.getUnreadNotificationsCountForUser(userId);
                return ResponseEntity.ok(count);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(0L);
            }
        } else if (userIdObj instanceof Integer) {
            long count = notificationService.getUnreadNotificationsCountForUser((Integer) userIdObj);
            return ResponseEntity.ok(count);
        }
        return ResponseEntity.status(401).body(0L);
    }

    @PostMapping("/markAsRead/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationsAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/markAllAsRead")
    public ResponseEntity<String> markAllAsRead(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof String) {
            try {
                int userId = Integer.parseInt((String) userIdObj);
                notificationService.markAllNotificationsAsReadForUser(userId);
                return ResponseEntity.ok("All notifications marked as read");
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }
        } else if (userIdObj instanceof Integer) {
            notificationService.markAllNotificationsAsReadForUser((Integer) userIdObj);
            return ResponseEntity.ok("All notifications marked as read");
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }
    @GetMapping("/staff")
    public ResponseEntity<List<Notifications>> getStaffNotifications(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (staffId != null && userRole != null && (userRole.equalsIgnoreCase("ROLE_DOCTOR") || userRole.equalsIgnoreCase("ROLE_LAB TECHNICIAN")|| userRole.equalsIgnoreCase("ROLE_ADMIN"))) {
            List<Notifications> notifications = notificationService.getNotificationsForStaffUser(staffId, userRole);
            return ResponseEntity.ok(notifications);
        }
        return ResponseEntity.status(401).body(List.of()); // Unauthorized or invalid role
    }

    @GetMapping("/staff/unread/count")
    public ResponseEntity<Long> getStaffUnreadNotificationCount(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (staffId != null && userRole != null && (userRole.equalsIgnoreCase("ROLE_DOCTOR") || userRole.equalsIgnoreCase("ROLE_LAB TECHNICIAN")|| userRole.equalsIgnoreCase("ROLE_ADMIN"))) {
            long count = notificationService.getUnreadNotificationsCountForStaffUser(staffId, userRole);
            return ResponseEntity.ok(count);
        }
        return ResponseEntity.status(401).body(0L); // Unauthorized or invalid role
    }

    @PostMapping("/staff/markAsRead/{notificationId}")
    public ResponseEntity<String> markStaffNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationsAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/staff/markAllAsRead")
    public ResponseEntity<String> markAllStaffNotificationsAsRead(HttpSession session) {
        String staffId = (String) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (staffId != null && userRole != null && (userRole.equalsIgnoreCase("ROLE_DOCTOR") || userRole.equalsIgnoreCase("ROLE_LAB TECHNICIAN")|| userRole.equalsIgnoreCase("ROLE_ADMIN"))) {
            notificationService.markAllNotificationsAsReadForStaff(staffId);
            return ResponseEntity.ok("All notifications marked as read");
        }
        return ResponseEntity.status(401).body("Unauthorized or invalid role");
    }
}
