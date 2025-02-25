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

    public LabOrderDTO getLabOrderDetails(int orderID) {
        LabOrder labOrder = labOrderRepository.findById(orderID).orElse(null);
        return convertToDTO(labOrder); // Use your existing convertToDTO method
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
        dto.userID = labOrder.getUserID().getUserID();
        dto.userName=labOrder.getUserID().getName();
        dto.doctorID = labOrder.getDoctorID().getStaffID();
        dto.doctorName=labOrder.getDoctorID().getStaffName();
        dto.appointmentID = labOrder.getAppointmentID().getAppointmentID();
        dto.urgency = labOrder.getUrgency();
        dto.orderDate = labOrder.getOrderDate();

        if (labOrder.getLabResults() != null) {
            dto.labResults = labOrder.getLabResults().stream()
                    .map(labResultsService::convertToDTO) // Correct call!
                    .collect(Collectors.toList());
        }

        return dto;
    }



}
