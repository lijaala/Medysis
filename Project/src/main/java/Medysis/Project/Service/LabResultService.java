package Medysis.Project.Service;

import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.DTO.LabResultDTO;
import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class LabResultService {


    @Autowired
    public StaffRepository staffRepository;
    @Autowired
    public LabOrderRepository labOrderRepository;

    @Autowired
    private LabResultRepository labResultRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;



    public List<LabResultDTO> getAllLabResults() {
        List<LabResults> labResults = labResultRepository.findAll();
        return labResults.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LabResultDTO convertToDTO(LabResults labResults) {  // Correct signature
        if (labResults == null) return null;

        LabResultDTO dto = new LabResultDTO();
        dto.reportId = labResults.getReportId();
        dto.userID = labResults.getUserID().getUserID();
        dto.doctorID = labResults.getDoctorID().getStaffID();
        dto.appointmentID = labResults.getAppointmentID().getAppointmentID();
        dto.orderID = labResults.getOrderID().getOrderID();
        dto.testID = labResults.getTestID().getTestID();
        dto.testName=labResults.getTestID().getTestName();
        dto.normalRange=labResults.getTestID().getNormalRange();
        dto.measurementUnit=labResults.getTestID().getMeasurementUnit();
        dto.resultValue=labResults.getResultValue();
        dto.notes = labResults.getNotes();
        if (labResults.getLabTechnicianID() != null) {
            dto.labTechnicianID = labResults.getLabTechnicianID().getStaffName();
        } else {
            dto.labTechnicianID = null; // Or set to an empty string "" or a default value like "Not Assigned"
        }

        return dto;
    }


    public void updateLabResult(int orderId, int testId, Double resultValue, String notes, HttpSession session) {
        String labTechnicianId = (String) session.getAttribute("userId");

        LabOrder labOrder = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Lab order not found with ID " + orderId));

        LabTests labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Lab test not found with ID " + testId));

        // ✅ Fix: Handle Optional<LabResults>
        LabResults labResult = labResultRepository.findByOrderIDAndTestID(labOrder, labTest)
                .orElseThrow(() -> new NoSuchElementException(
                        "Lab Result not found for Order ID: " + orderId + " and Test ID: " + testId));
        labResult.setOrderID(labOrder);
        labResult.setTestID(labTest);

        // Fetch Lab Technician
        Staff labTechnician = staffRepository.findById(labTechnicianId)
                .orElseThrow(() -> new IllegalArgumentException("Lab technician not found"));

        boolean hasChanges = (labResult.getResultValue() == null || !labResult.getResultValue().equals(resultValue))
                || (labResult.getNotes() == null || !labResult.getNotes().equals(notes));

        if (hasChanges) {
            labResult.setResultValue(resultValue);
            labResult.setNotes(notes);
            labResult.setLabTechnicianID(labTechnician);
            labResultRepository.save(labResult);
            sendLabResultNotification(labResult);
        }


        List<LabResults> resultsForOrder = labResultRepository.findByOrderID(labOrder);

        // Check if all results have values
        boolean allResultsEntered = resultsForOrder.stream()
                .allMatch(result -> result.getResultValue() != null);

        // Update order status
        String newStatus = allResultsEntered ? "Completed" : "Pending";
        labOrder.setLabStatus(newStatus);
        System.out.println("Updating Order Status for Order ID " + orderId + " to: " + newStatus);
        labOrderRepository.save(labOrder);
    }
    private void sendLabResultNotification(LabResults labResult) {
        User patient = userRepository.findById(labResult.getUserID().getUserID())
                .orElse(null); // Handle case where user might not be found

        if (patient != null) {
            String message = String.format("Lab result updated for Order ID: %d, Test: %s. Please check your reports.",
                    labResult.getOrderID().getOrderID(), labResult.getTestID().getTestName());
            notificationService.createUserNotifications(patient.getUserID(), message, "lab_result");
        } else {
            System.err.println("Could not find patient for Lab Result ID: " + labResult.getReportId());
        }
    }




}
