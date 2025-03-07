package Medysis.Project.Service;

import Medysis.Project.DTO.RoleDTO;
import Medysis.Project.DTO.UserDTO;
import Medysis.Project.Model.Availability;
import Medysis.Project.Model.Role;
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
    private UploadImageService uploadImageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, AvailabilityRepository availabilityRepository,
                       RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.availabilityRepository = availabilityRepository;
        this.roleService = roleService;
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
        return userRepository.findAll();
    }

    @Transactional
    public boolean updateUser(Integer userID, String name, String phone, Integer age,
                              String gender, String address, MultipartFile image, String editorId) {
        Optional<User> existingUserOpt = userRepository.findById(userID);

        if (existingUserOpt.isEmpty()) {
            return false; // User not found
        }

        User existingUser = existingUserOpt.get();


        if (name != null) existingUser.setName(name);
        if (phone != null) existingUser.setPhone(phone);
        if (age != null) existingUser.setAge(age);
        if (gender != null) existingUser.setGender(gender);
        if (address != null) existingUser.setAddress(address);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = uploadImageService.saveImage(image);
                existingUser.setImage(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // Track update metadata
        existingUser.setUpdated_at(LocalDateTime.now());
        existingUser.setUpdatedBy(editorId);

        userRepository.save(existingUser);
        return true;
    }


    public User getUserById(Integer userID) {
        return userRepository.findById(userID).orElse(null);
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
}










