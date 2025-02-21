package Medysis.Project.Service;


import Medysis.Project.DTO.RoleDTO;
import Medysis.Project.DTO.StaffDTO;
import Medysis.Project.Model.Role;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRepository userRepository;

    public StaffService( StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Staff save(Staff staff) {

        if (staffRepository.findByStaffEmail(staff.getStaffEmail()).isPresent()) {
            throw new IllegalArgumentException("Staff with email address  already exists");

        }
        String staffId = generateStaffId(staff.getStaffName());
        String password = passwordEncoder.encode(staffId);
        staff.setStaffID(staffId);
        staff.setPassword(password);
        LocalDateTime now= LocalDateTime.now();
        staff.setAddedOn(now);
        staff.setLastUpdated(now);

        // Save the staff entity
        return staffRepository.save(staff);
    }
    private String generateUniqueStaffId(String fullName) {
        String staffID;
        do {
            staffID = generateStaffId(fullName);
        } while (staffRepository.existsByStaffID(staffID)); // Check for uniqueness in the database

        return staffID;
    }

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
            String specialChars = "!@#$%^&*";
            char specialChar1 = specialChars.charAt(random.nextInt(specialChars.length()));
            char specialChar2 = specialChars.charAt(random.nextInt(specialChars.length()));

            // Combine all parts to form the staff ID
            return initials + digits + specialChar1 + specialChar2;
        }

    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByStaffEmail(email);
    }

    public StaffDTO convertToDTO(Staff staff) {
        if (staff == null) return null;

        StaffDTO dto = new StaffDTO();
        dto.staffID = staff.getStaffID();    // Only staffID
        dto.staffName = staff.getStaffName();  // Only staffName

        dto.startTime = staff.getStartTime();
        dto.endTime = staff.getEndTime();

        return dto; // That's it! No role or other fields
    }
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff updateStaffAvailability(String staffID, Staff updatedStaff) {
        Optional<Staff> existingStaffOpt = staffRepository.findById(staffID);

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
    public Staff getProfile(String staffId) {
        return staffRepository.findById(staffId).orElse(null);
    }

    public Staff updateProfile(String staffId, Staff updatedStaff) {
        Optional<Staff> optionalStaff = staffRepository.findById(staffId);

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
        if ("DOCTOR_DOCTOR".equals(existingStaff.getRole().getRole())) {
            if (updatedStaff.getStartTime() != null) {
                existingStaff.setStartTime(updatedStaff.getStartTime());
            }
            if (updatedStaff.getEndTime() != null) {
                existingStaff.setEndTime(updatedStaff.getEndTime());
            }
        }

        return staffRepository.save(existingStaff);
    }

}


