package Medysis.Project.Service;

import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import Medysis.Project.Security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public CustomUserDetailsService(UserRepository userRepository, StaffRepository staffRepository) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check if email exists in Users table
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return new CustomUserDetails(userOptional.get());
        }

        // Check if email exists in Staff table
        Optional<Staff> staffOptional = staffRepository.findByStaffEmail(email);
        if (staffOptional.isPresent()) {
            return new CustomUserDetails(staffOptional.get());
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

}
