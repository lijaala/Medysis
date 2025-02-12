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
                .map(labResult -> {
                    LabOrder labOrder = labResult.getOrderID(); // Get LabOrder directly
                    LabOrderDTO labOrderDTO = convertLabOrderToDTO(labOrder); // Convert to DTO
                    return convertToDTO(labResult, labOrderDTO); // Use DTO in conversion
                })
                .collect(Collectors.toList());
    }


    private LabOrderDTO convertLabOrderToDTO(LabOrder labOrder) {
        if (labOrder == null) return null;

        LabOrderDTO dto = new LabOrderDTO();
        dto.orderID = labOrder.getOrderID();
        dto.userID = userService.convertToDTO(labOrder.getUserID());
        dto.doctorID = staffService.convertToDTO(labOrder.getDoctorID());
        dto.appointmentID = appointmentService.convertToDTO(labOrder.getAppointmentID());
        dto.urgency = labOrder.getUrgency();
        dto.orderDate = labOrder.getOrderDate();
        return dto;
    }

    public LabResultDTO convertToDTO(LabResults labResults, LabOrderDTO labOrderDTO) {
        if (labResults == null) return null;

        LabResultDTO dto = new LabResultDTO();
        dto.reportId = labResults.getReportId();
        dto.userID = userService.convertToDTO(labResults.getUserID());
        dto.doctorID = staffService.convertToDTO(labResults.getDoctorID());
        dto.appointmentID = appointmentService.convertToDTO(labResults.getAppointmentID());
        dto.orderID = labOrderDTO; // Use the provided LabOrderDTO
        dto.testID = labTestsService.convertToDTO(labResults.getTestID());
        return dto;
    }

}
