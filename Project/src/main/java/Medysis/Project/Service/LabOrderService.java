package Medysis.Project.Service;

import Medysis.Project.DTO.*;
import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class LabOrderService {
    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private LabResultRepository labResultsRepository;

    @Autowired
    private LabTestRepository labTestsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AppointmentService appointmentService;



    @Autowired
    private LabResultService labResultsService;


    public LabOrder createLabOrder(int appointmentId, int userId, String staffId,  String urgency, List<Integer> testIds) {
        LabOrder labOrder = new LabOrder();
        User user = userRepository.findById(userId).orElse(null);
        Staff staffID = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        labOrder.setUserID(user);
        labOrder.setDoctorID(staffID);
        labOrder.setAppointmentID(appointment);
        labOrder.setOrderDate(LocalDate.now()); // Or get from the request
        labOrder.setUrgency(urgency);
        labOrder.setLabStatus("Pending");
        labOrderRepository.save(labOrder);



        for (Integer testId : testIds) {
            LabResults labResult = new LabResults();
            labResult.setOrderID(labOrder);
            labResult.setUserID(user);
            labResult.setDoctorID(staffID);
            labResult.setAppointmentID(appointment);
            LabTests labTest = labTestsRepository.findById(testId).orElse(null);
            labResult.setTestID(labTest);
            labResultsRepository.save(labResult);
        }

        return labOrder; // Or return a DTO if you prefer
    }

//    public List<LabOrder> getAllLabOrders() {
//        return labOrderRepository.findAll();
//    }
    public LabOrder getLabOrderDetails(int orderID) {
        return labOrderRepository.findById(orderID).orElse(null);
    }

    public List<LabOrderDTO> getAllLabOrders() {
        List<LabOrder> labOrders = labOrderRepository.findAll();
        return labOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LabOrderDTO convertToDTO(LabOrder labOrder) {
        if (labOrder == null) return null;

        LabOrderDTO dto = new LabOrderDTO();
        dto.orderID = labOrder.getOrderID();
        dto.userID = convertUserToUserDTO(labOrder.getUserID());
        dto.doctorID = convertStaffToStaffDTO(labOrder.getDoctorID());
        dto.appointmentID = convertAppointmentToAppointmentDTO(labOrder.getAppointmentID());
        dto.urgency = labOrder.getUrgency();
        dto.orderDate = labOrder.getOrderDate();

        if (labOrder.getLabResults() != null) {
            dto.labResults = labOrder.getLabResults().stream()
                    .map(labResult -> labResultsService.convertToDTO(labResult, dto))
                    .collect(Collectors.toList());
        }

        return dto;
    }

    private UserDTO convertUserToUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.userID = user.getUserID();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.address = user.getAddress();
        dto.gender = user.getGender();
        dto.age = user.getAge();
        dto.image = user.getImage();

        if (user.getRole() != null) {
            dto.role = convertRoleToRoleDTO(user.getRole());
        } else {
            dto.role = null;
        }

        dto.verified = user.isVerified();
        dto.created_at = user.getCreated_at();
        dto.updated_at = user.getUpdated_at();

        return dto;
    }

    private StaffDTO convertStaffToStaffDTO(Staff staff) {
        if (staff == null) return null;
        StaffDTO dto = new StaffDTO();
        dto.staffID = staff.getStaffID();
        dto.staffName = staff.getStaffName();
        dto.staffEmail = staff.getStaffEmail();
        dto.staffPhone = staff.getStaffPhone();
        dto.staffAddress = staff.getStaffAddress();
        dto.gender = staff.getGender();
        dto.age = staff.getAge();
        dto.image = staff.getImage();

        if (staff.getRole() != null) {
            dto.role = convertRoleToRoleDTO(staff.getRole());
        } else {
            dto.role = null;
        }

        dto.addedOn = staff.getAddedOn();
        dto.lastActive = staff.getLastActive();
        dto.lastUpdated = staff.getLastUpdated();
        dto.startTime = staff.getStartTime();
        dto.endTime = staff.getEndTime();

        return dto;
    }

    private AppointmentDTO convertAppointmentToAppointmentDTO(Appointment appointment) {
        if (appointment == null) return null;
        AppointmentDTO dto = new AppointmentDTO();
        dto.appointmentID = appointment.getAppointmentID();
        if (appointment.getPatientID() != null) {
            dto.patientID = convertUserToUserDTO(appointment.getPatientID());
        }
        if (appointment.getDoctorID() != null) {
            dto.doctorID = convertStaffToStaffDTO(appointment.getDoctorID());
        }
        dto.appDate = appointment.getAppDate();
        dto.appTime = appointment.getAppTime();
        dto.status = appointment.getStatus();
        dto.followUpDate = appointment.getFollowUpDate();
        return dto;
    }

    private RoleDTO convertRoleToRoleDTO(Role role) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.roleID = role.getRoleID();
        dto.role = role.getRole();
        return dto;
    }

}
