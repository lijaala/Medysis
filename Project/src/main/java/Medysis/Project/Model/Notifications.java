package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userID")
    private User userId;

    @ManyToOne
    @JoinColumn(name = "staff_id",referencedColumnName = "staffID")
    private Staff staffId;

    private String message;

    private LocalDateTime createdAt;

    private String type; // e.g., "appointment", "lab", "prescription", "account"

    private String status = "unread"; // "unread", "read"


    // Constructors
    public Notifications() {
        this.createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Kathmandu")); // Set Nepal time on creation
    }

    public Notifications(User user, String message, String type) {
        this.userId = user;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Kathmandu"));
    }

    public Notifications(Staff staff, String message, String type) {
        this.staffId = staff;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Kathmandu"));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Staff getStaffId() {
        return staffId;
    }

    public void setStaffId(Staff staffId) {
        this.staffId = staffId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "Notifications{" +
                "id=" + id +
                ", userId=" + (userId != null ? userId.getUserID() : null) +
                ", staffId='" + (staffId != null ? staffId.getStaffID() : null) + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
