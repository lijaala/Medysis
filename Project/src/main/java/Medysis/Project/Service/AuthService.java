package Medysis.Project.Service;

import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import jakarta.servlet.http.HttpSession;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AuthService {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private EmailService emailService;



    public String authenticate(String email, String password, HttpSession session) throws Exception {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            return authenticateUser(userOptional.get(), password, session);
        }

        Optional<Staff> staffOptional = staffService.findByEmail(email);
        if (staffOptional.isPresent()) {
            return authenticateStaff(staffOptional.get(), password, session);
        }

        throw new Exception("User not found");
    }
    private String authenticateUser(User user, String password, HttpSession session) throws Exception {
        if (!user.isVerified()) {
            emailService.sendVerificationEmail(user);
            throw new Exception("User not verified. Verification email sent.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid credentials");
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole().getRole());
        return "/appointment";
    }

    private String authenticateStaff(Staff staff, String password, HttpSession session) throws Exception {
        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new Exception("Invalid credentials");
        }

        session.setAttribute("userId", staff.getStaffID());
        session.setAttribute("userEmail", staff.getStaffEmail());
        session.setAttribute("userRole", staff.getRole().getRole() );
        return "/home";
    }
}