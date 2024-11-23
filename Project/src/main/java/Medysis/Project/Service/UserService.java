package Medysis.Project.Service;

import Medysis.Project.Model.User;
import Medysis.Project.Repository.UserRepository;
import Medysis.Project.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = new EmailService();
    }

    public void registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setRole(Role.User);
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
}





