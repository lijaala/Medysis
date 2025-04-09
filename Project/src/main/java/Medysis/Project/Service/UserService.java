package Medysis.Project.Service;

import Medysis.Project.DTO.UserDTO;
import Medysis.Project.Model.Role;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.AvailabilityRepository;
import Medysis.Project.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import java.util.*;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final EmailService emailService;

    @Autowired
    AvailabilityRepository availabilityRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UploadImageService uploadImageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, AvailabilityRepository availabilityRepository,
                       RoleService roleService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.availabilityRepository = availabilityRepository;
        this.roleService = roleService;
        this.notificationService = notificationService;
    }

    public void registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        Role userRole = roleService.findRoleById(4);
        user.setRole(userRole);
        user.setCreated_at(now);
        user.setUpdated_at(now);

        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);


        userRepository.save(user);
        emailService.sendVerificationEmail(user);

    }

    public User findUserByVerificationCode(String verificationCode) {
        return userRepository.findByVerificationCode(verificationCode);
    }

    public boolean verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if (user != null && !user.isVerified()) {
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserDTO convertToDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.userID = user.getUserID();  // Only userID
        dto.name = user.getName();      // Only name

        return dto; // That's it! No role or other fields
    }


    public List<User> getAllUsers() {
        return userRepository.findAllActiveUsers();
    }

    @Transactional
    public boolean updateUser(Integer userID, String name, String phone, Integer age,
                              String gender, String address, MultipartFile image, String editorId, Double weight, String bloodType) {
        Optional<User> existingUserOpt = userRepository.findActiveById(userID);

        if (existingUserOpt.isEmpty()) {
            return false; // User not found
        }

        User existingUser = existingUserOpt.get();
        boolean updated = false;


        if (name != null && !name.equals(existingUser.getName())) {
            existingUser.setName(name);
            updated = true;
        }
        if (phone != null && !phone.equals(existingUser.getPhone())) {
            existingUser.setPhone(phone);
            updated = true;
        }
        if (age != null && !age.equals(existingUser.getAge())) {
            existingUser.setAge(age);
            updated = true;
        }
        if (gender != null && !gender.equals(existingUser.getGender())) {
            existingUser.setGender(gender);
            updated = true;
        }
        if (address != null && !address.equals(existingUser.getAddress())) {
            existingUser.setAddress(address);
            updated = true;
        }
        if (weight != null && !weight.equals(existingUser.getWeight())) {
            existingUser.setWeight(weight);
            updated = true;
        }
        if (bloodType != null && !bloodType.equals(existingUser.getBloodType())) {
            existingUser.setBloodType(bloodType);
            updated = true;
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = uploadImageService.saveImage(image);
                existingUser.setImage(imageUrl);
                updated = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        if (updated) {
            existingUser.setUpdated_at(LocalDateTime.now());
            existingUser.setUpdatedBy(editorId);
            userRepository.save(existingUser);
            notificationService.createUserNotifications(userID, "Your account details have been updated.", "account");
            return true;
        }
        return false;
    }



    public User getUserById(Integer userID) {
        return userRepository.findActiveById(userID).orElse(null);
    }

    public void generatePasswordResetToken(User user) {
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15); // Token valid for 30 minutes

        user.setResetTokenExpiry(expiryTime);
        user.setResetToken(resetToken);
        userRepository.save(user);

        String resetUrl = "http://localhost:8081/forgotPassword?token=" + resetToken;
        emailService.sendPasswordResetEmail(user, resetUrl);
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {

        Optional<User> userOptional = userRepository.findByResetToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null); // Clear the reset token
            userRepository.save(user);

            return true;
        }
        return false;
    }
    public boolean resetPassword(Integer userId, String currentPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findActiveById(userId);

        if (optionalUser.isEmpty()) {
            return false; // Staff not found
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Current password is incorrect
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        notificationService.createUserNotifications(userId, "Your password has been changed.", "account");

        return true; // Password successfully updated
    }
    public boolean softDeleteUser(Integer userId) {
        try {
            Optional<User> userOptional = userRepository.findActiveById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("Found user: " + user.getEmail()); // Add logging
                user.setDeleted(true);
                user.setUpdated_at(LocalDateTime.now(java.time.Clock.systemDefaultZone().withZone(java.time.ZoneId.of("Asia/Kathmandu"))));
                userRepository.save(user);
                System.out.println("User soft deleted successfully: " + user.getEmail()); // Add logging
                emailService.sendAccountDeletionEmail(user); // Call EmailService

                return true;
            } else {
                System.out.println("User not found with ID: " + userId); // Add logging
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during soft delete for user ID " + userId + ": " + e.getMessage()); // Print the error message
            e.printStackTrace(); // Print the full stack trace
            return false;
        }
    }
    public Map<String, Object> getUserProfileDetails(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId); // Assuming your User entity has an ID field

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("gender", user.getGender()); // Assuming User entity has getGender()
            profileInfo.put("age", user.getAge());       // Assuming User entity has getAge()
            profileInfo.put("bloodType", user.getBloodType()); // Assuming User entity has getBloodType()
            profileInfo.put("weight", user.getWeight()); // Assuming User entity has getWeight()
            return profileInfo;
        }
        return null;
    }


}










