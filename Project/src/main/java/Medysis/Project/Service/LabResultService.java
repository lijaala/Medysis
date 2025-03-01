package Medysis.Project.Service;

import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.DTO.LabResultDTO;
import Medysis.Project.Model.LabOrder;
import Medysis.Project.Model.LabResults;
import Medysis.Project.Model.LabTests;
import Medysis.Project.Model.Staff;
import Medysis.Project.Repository.LabOrderRepository;
import Medysis.Project.Repository.LabResultRepository;
import Medysis.Project.Repository.LabTestRepository;
import Medysis.Project.Repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
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
                .orElse(new LabResults(labOrder, labTest));

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






}
