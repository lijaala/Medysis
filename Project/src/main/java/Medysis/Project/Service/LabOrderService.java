package Medysis.Project.Service;

import Medysis.Project.DTO.*;
import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

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



    @Transactional
    public LabOrder createLabOrder(int appointmentId, int userId, String staffId, String urgency, List<Integer> testIds) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        // Check if a LabOrder already exists for this appointment
        List<LabOrder> existingLabOrders = labOrderRepository.findByAppointmentID(appointment);

        if (!existingLabOrders.isEmpty()) {
            // If a LabOrder exists, update the first one found
            LabOrder existingLabOrder = existingLabOrders.get(0);
            return updateExistingLabOrder(existingLabOrder, userId, staffId, urgency, testIds);
        } else {
            // If no LabOrder exists, create a new one
            LabOrder labOrder = new LabOrder();
            User user = userRepository.findById(userId).orElse(null);
            Staff staffID = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

            labOrder.setUserID(user);
            labOrder.setDoctorID(staffID);
            labOrder.setAppointmentID(appointment);
            labOrder.setOrderDate(LocalDate.now());
            labOrder.setUrgency(urgency);
            labOrder.setLabStatus("Pending");
            labOrderRepository.save(labOrder);

            // Create LabResults
            createLabResults(labOrder, userId, staffId, appointment, testIds);
            return labOrder;
        }
    }

    @Transactional
    public LabOrder updateLabOrder(int orderId, int appointmentId, int userId, String staffId, String urgency, List<Integer> testIds) {
        LabOrder labOrder = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Lab Order not found with ID: " + orderId));
        return updateExistingLabOrder(labOrder, userId, staffId, urgency, testIds);
    }

    @Transactional
    private LabOrder updateExistingLabOrder(LabOrder labOrder, int userId, String staffId, String urgency, List<Integer> testIds) {
        User user = userRepository.findById(userId).orElse(null);
        Staff staffID = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));
        Appointment appointment = labOrder.getAppointmentID(); // Get existing appointment

        labOrder.setUserID(user);
        labOrder.setDoctorID(staffID);
        labOrder.setOrderDate(LocalDate.now()); // Consider if you want to update this
        labOrder.setUrgency(urgency);
        labOrder.setLabStatus("Pending"); // Or keep the existing status
        labOrderRepository.save(labOrder);

        // Remove existing lab results for this order
        List<LabResults> existingResults = labResultsRepository.findByOrderID(labOrder);
        labResultsRepository.deleteAll(existingResults);

        // Add new lab results based on the provided test IDs
        createLabResults(labOrder, userId, staffId, appointment, testIds);

        return labOrder;
    }

    private void createLabResults(LabOrder labOrder, int userId, String staffId, Appointment appointment, List<Integer> testIds) {
        User user = userRepository.findById(userId).orElse(null);
        Staff staffID = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

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

    public List<LabOrderDTO> getLabOrdersByUserId(User userId) {
        List<LabOrder> labOrders = labOrderRepository.findByUserID(userId);
        return labOrders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LabOrderDTO> getLabOrdersByAppointmentId(int appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        List<LabOrder> labOrders = labOrderRepository.findByAppointmentID(appointment);
        return labOrders.stream().map(this::convertToDTO).collect(Collectors.toList());
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
        dto.labStatus = labOrder.getLabStatus();

        if (labOrder.getLabResults() != null) {
            dto.labResults = labOrder.getLabResults().stream()
                    .map(labResultsService::convertToDTO) // Correct call!
                    .collect(Collectors.toList());
        }

        return dto;
    }



}
