package Medysis.Project.Service;

import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;




    public String authenticate(String email, String password, HttpSession session) {
        Optional<User> userOptional = userService.findByEmail(email);
        Optional<Staff> staffOptional = staffService.findByEmail(email);

        if (userOptional.isPresent()) {
            return authenticateUser(userOptional.get(), password, session);
        } else if (staffOptional.isPresent()) {
            return authenticateStaff(staffOptional.get(), password, session);
        } else {
            return "User not found";
        }
    }

    private String authenticateUser(User user, String password, HttpSession session) {
        if (!user.isVerified()) {
            emailService.sendVerificationEmail(user);
            return "User not verified. Verification email sent.";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "Invalid credentials";
        }

        return authenticateAndSetSession(user.getEmail(), password, String.valueOf(user.getUserID()), "ROLE_PATIENT", session);
    }

    private String authenticateStaff(Staff staff, String password, HttpSession session) {
        if (!passwordEncoder.matches(password, staff.getPassword())) {
            return "Incorrect password or email";
        }

        return authenticateAndSetSession(staff.getStaffEmail(), password, staff.getStaffID(), staff.getRole().getRole(), session);
    }

    private String authenticateAndSetSession(String email, String password, String userId, String role, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            session.setAttribute("userId", userId);
            session.setAttribute("userEmail", email);
            session.setAttribute("userRole", role);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return "success";
        } catch (BadCredentialsException e) {
            return "Incorrect password or email";
        }
    }

}