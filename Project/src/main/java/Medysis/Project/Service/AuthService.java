package Medysis.Project.Service;

import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import jakarta.servlet.http.HttpSession;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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



    public String authenticate(String email, String password, HttpSession session) throws Exception {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        Optional<User> userOptional = userService.findByEmail(email);
        Optional<Staff> staffOptional = staffService.findByEmail(email);

        if (userOptional.isPresent()) {
            return authenticateUser(userOptional.get(),userDetails, password, session);
        }
        else if (staffOptional.isPresent()) {
            return authenticateStaff(staffOptional.get(), userDetails,password, session);

        }
        else {
            return "User not found";
        }




    }
    private String authenticateUser(User user, UserDetails userDetails,String password, HttpSession session) throws Exception {
        if (!user.isVerified()) {
            emailService.sendVerificationEmail(user);
            throw new Exception("User not verified. Verification email sent.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid credentials");
        }
        setSessionAndSecurityContext(userDetails, user.getId().toString(), user.getEmail(), user.getRole().getRole(), session);
        return "/home";

//        session.setAttribute("userId", user.getId());
//        session.setAttribute("userEmail", user.getEmail());
//        session.setAttribute("userRole", user.getRole().getRole());
//        return "/home" ;
    }

    private String authenticateStaff(Staff staff,UserDetails userDetails,String password, HttpSession session) throws Exception {
        System.out.println("Staff Email: " + staff.getStaffEmail());
        System.out.println("Stored Password: " + staff.getPassword());
        System.out.println("Entered Password: " + password);
        System.out.println("Password Match: " + passwordEncoder.matches(password, staff.getPassword()));


        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new Exception("Invalid credentials");
        }
        setSessionAndSecurityContext(userDetails, staff.getStaffID(), staff.getStaffEmail(), staff.getRole().getRole(), session);
        return "/home";
//        session.setAttribute("userId", staff.getStaffID());
//        session.setAttribute("userEmail", staff.getStaffEmail());
//        session.setAttribute("userRole", staff.getRole().getRole() );
//        return "/home";
    }
    private void setSessionAndSecurityContext(UserDetails userDetails, String userId, String email, String role, HttpSession session) {
        session.setAttribute("userId", userId);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


    }
}
