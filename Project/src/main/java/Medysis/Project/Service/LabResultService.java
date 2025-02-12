package Medysis.Project.Service;

import Medysis.Project.DTO.LabOrderDTO;
import Medysis.Project.DTO.LabResultDTO;
import Medysis.Project.Model.LabOrder;
import Medysis.Project.Model.LabResults;
import Medysis.Project.Repository.LabResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabResultService {

    @Autowired
    private UserService userService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AppointmentService appointmentService;



    @Autowired
    private LabTestService labTestsService;

    @Autowired
    private LabResultRepository labResultRepository;

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
        return dto;
    }
}
