package Medysis.Project.Service;

import Medysis.Project.DTO.RoleDTO;
import Medysis.Project.DTO.UserDTO;
import Medysis.Project.Model.Availability;
import Medysis.Project.Model.Role;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.AvailabilityRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public boolean updateUser(Integer userID, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(userID);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Update the relevant fields
            existingUser.setName(updatedUser.getName());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setVerified(updatedUser.isVerified());

            // Save the updated user
            userRepository.save(existingUser);
            return true;
        }
        return false; // User not found
    }

    }








