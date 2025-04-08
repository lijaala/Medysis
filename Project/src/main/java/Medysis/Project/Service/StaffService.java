package Medysis.Project.Service;


import Medysis.Project.DTO.StaffDTO;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UploadImageService uploadImageService;

    @Autowired
    private final EmailService emailService;
    @Autowired
    private final NotificationService notificationService;



    public StaffService( StaffRepository staffRepository, PasswordEncoder passwordEncoder, UploadImageService uploadImageService, EmailService emailService, NotificationService notificationService) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadImageService = uploadImageService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    //staff registration method
    public Staff save(Staff staff) {

        if (staffRepository.findByStaffEmail(staff.getStaffEmail()).isPresent()) {
            throw new IllegalArgumentException("Staff with email address  already exists");

        }
        String staffId = generateUniqueStaffId(staff.getStaffName());
        String password = passwordEncoder.encode(staffId);
        staff.setStaffID(staffId);
        staff.setPassword(password);
        LocalDateTime now= LocalDateTime.now();
        staff.setAddedOn(now);
        staff.setLastUpdated(now);

        Staff savedStaff = staffRepository.save(staff);
        notifyAdminsNewStaffAdded(savedStaff);

        // Save the staff entity
        return staffRepository.save(staff);
    }
    private void notifyAdminsNewStaffAdded(Staff staff) {
        List<Staff> admins = staffRepository.findByRoleRoleID(1);
        String notificationMessage = "New staff member '" + staff.getStaffName() + "' has been added.";
        for (Staff admin : admins) {
            notificationService.createStaffNotifications(admin.getStaffID(), notificationMessage, "staff_added");
        }
    }
    //generating unique ids
    private String generateUniqueStaffId(String fullName) {
        String staffID;
        do {
            staffID = generateStaffId(fullName);
        } while (staffRepository.existsByStaffID(staffID)); // Check for uniqueness in the database

        return staffID;
    }
    // generating staff ID
    private String generateStaffId(String fullName) {
        // Split full name by spaces
        String[] nameParts = fullName.trim().split("\\s+");

        // Get the first letter of the first and last name
        String firstInitial = nameParts.length > 0 ? nameParts[0].substring(0, 1).toUpperCase() : "X";
        String lastInitial = nameParts.length > 1 ? nameParts[1].substring(0, 1).toUpperCase() : "X";

        // Combine initials
        String initials = firstInitial + lastInitial;
        // Generate three random digits
        Random random = new Random();
        String digits = String.format("%03d", random.nextInt(1000));

        // Generate two random special characters
        String specialChars = "!@#$^&*";
        char specialChar1 = specialChars.charAt(random.nextInt(specialChars.length()));
        char specialChar2 = specialChars.charAt(random.nextInt(specialChars.length()));

        // Combine all parts to form the staff ID
            return initials + digits + specialChar1 + specialChar2;
        }

        //finding staff by Email
    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByStaffEmail(email);
    }

    //DTO conversion
    public StaffDTO convertToDTO(Staff staff) {
        if (staff == null) return null;

        StaffDTO dto = new StaffDTO();
        dto.staffID = staff.getStaffID();
        dto.staffName = staff.getStaffName();
        dto.startTime = staff.getStartTime();
        dto.endTime = staff.getEndTime();

        return dto;
    }
    // get all staff
    public List<Staff> getAllStaff() {
        return staffRepository.findAllActiveStaff();
    }
    //updating available time of staff
    public Staff updateStaffAvailability(String staffID, Staff updatedStaff) {
        Optional<Staff> existingStaffOpt = staffRepository.findActiveById(staffID);

        if (existingStaffOpt.isPresent()) {
            Staff existingStaff = existingStaffOpt.get();

            // Ensure only start time and end time are updated
            existingStaff.setStartTime(updatedStaff.getStartTime());
            existingStaff.setEndTime(updatedStaff.getEndTime());

            return staffRepository.save(existingStaff);
        } else {
            throw new RuntimeException("Staff with ID " + staffID + " not found.");
        }
    }
    //getting staff by ID
    public Staff getProfile(String staffId) {
        return staffRepository.findActiveById(staffId).orElse(null);
    }
    // staff uopdat eprofile
    public Staff updateProfile(String staffId, Staff updatedStaff) {
        Optional<Staff> optionalStaff = staffRepository.findActiveById(staffId);

        if (optionalStaff.isEmpty()) {
            throw new RuntimeException("Staff not found");
        }

        Staff existingStaff = optionalStaff.get();

        // Update only non-null and non-empty fields
        if (updatedStaff.getStaffName() != null) {
            existingStaff.setStaffName(updatedStaff.getStaffName());
        }
        if (updatedStaff.getStaffEmail() != null) {
            existingStaff.setStaffEmail(updatedStaff.getStaffEmail());
        }
        if (updatedStaff.getStaffPhone() != null) {
            existingStaff.setStaffPhone(updatedStaff.getStaffPhone());
        }
        if (updatedStaff.getStaffAddress() != null) {
            existingStaff.setStaffAddress(updatedStaff.getStaffAddress());
        }
        if (updatedStaff.getGender() != null) {
            existingStaff.setGender(updatedStaff.getGender());
        }
        if (updatedStaff.getAge() != null) {
            existingStaff.setAge(updatedStaff.getAge());
        }

        // Only update availability if the staff is a doctor
        if ("ROLE_DOCTOR".equals(existingStaff.getRole().getRole())) {
            if (updatedStaff.getStartTime() != null) {
                existingStaff.setStartTime(updatedStaff.getStartTime());
            }
            if (updatedStaff.getEndTime() != null) {
                existingStaff.setEndTime(updatedStaff.getEndTime());
            }
        }

        return staffRepository.save(existingStaff);
    }


    public boolean updateProfilePicture(String staffId, MultipartFile photo) {
        Optional<Staff> optionalStaff = staffRepository.findActiveById(staffId);

        if (optionalStaff.isEmpty()) {
            return false; // Staff not found
        }

        Staff staff = optionalStaff.get();
        String savedFileName = uploadImageService.saveImage(photo);

        if (savedFileName == null) {
            return false; // Image upload failed
        }

        staff.setImage(savedFileName);
        staffRepository.save(staff);
        return true; // Profile picture updated successfully
    }
    public void generatePasswordResetToken(Staff staff) {
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(30); // Token valid for 30 minutes

        staff.setResetToken(resetToken);
        staff.setResetTokenExpiry(expiryTime);

        staffRepository.save(staff);

        String resetUrl = "http://localhost:8081/forgotPassword?token=" + resetToken;
        emailService.sendPasswordResetEmail(staff, resetUrl);
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<Staff> staffOptional = staffRepository.findByResetToken(token);

        if (staffOptional.isPresent()) {
            Staff staff = staffOptional.get();
            staff.setPassword(passwordEncoder.encode(newPassword));
            staff.setResetToken(null); // Clear the reset token
            staffRepository.save(staff);
            return true;
        }
        return false;
    }
    public boolean resetPassword(String staffId, String currentPassword, String newPassword) {
        Optional<Staff> optionalStaff = staffRepository.findActiveById(staffId);

        if (optionalStaff.isEmpty()) {
            return false; // Staff not found
        }

        Staff staff = optionalStaff.get();

        if (!passwordEncoder.matches(currentPassword, staff.getPassword())) {
            return false; // Current password is incorrect
        }

        staff.setPassword(passwordEncoder.encode(newPassword));
        staffRepository.save(staff);
        return true; // Password successfully updated
    }

    public boolean softDeleteStaff(String staffIdToDelete, String deletedByUserId) {
        try {
            Optional<Staff> staffToDeleteOptional = staffRepository.findActiveById(staffIdToDelete);
            if (staffToDeleteOptional.isPresent()) {
                Staff staffToDelete = staffToDeleteOptional.get();
                System.out.println("Found staff to delete: " + staffToDelete.getStaffEmail() + " (ID: " + staffToDelete.getStaffID() + ")"); // Add logging
                staffToDelete.setDeleted(true);
                staffToDelete.setLastUpdated(LocalDateTime.now(java.time.Clock.systemDefaultZone().withZone(java.time.ZoneId.of("Asia/Kathmandu"))));
                staffToDelete.setLastUpdatedBy(deletedByUserId); // Record who deleted the account
                staffRepository.save(staffToDelete);
                System.out.println("Staff soft deleted successfully: " + staffToDelete.getStaffEmail() + " (ID: " + staffToDelete.getStaffID() + ") by user: " + deletedByUserId); // Add logging
                // emailService.sendAccountDeletionEmail(staffToDelete); // Call EmailService

                return true;
            } else {
                System.out.println("Staff not found with ID: " + staffIdToDelete); // Add logging
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during soft delete for staff ID " + staffIdToDelete + " by user " + deletedByUserId + ": " + e.getMessage()); // Print the error message
            e.printStackTrace(); // Print the full stack trace
            return false;
        }
    }

}


