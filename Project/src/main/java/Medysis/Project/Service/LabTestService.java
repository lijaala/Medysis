package Medysis.Project.Service;

import Medysis.Project.DTO.LabTestsDTO;
import Medysis.Project.Model.LabTests;
import Medysis.Project.Repository.LabTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabTestService {
    @Autowired
    private LabTestRepository labTestRepository;
    public List<LabTests> getAllLabTests(){
        return labTestRepository.findAll();
    }

    public LabTestsDTO convertToDTO(LabTests labTests) {
        if (labTests == null) return null; // Handle nulls

        LabTestsDTO dto = new LabTestsDTO();
        dto.testID = labTests.getTestID();
        dto.testName = labTests.getTestName();
        dto.normalRange = labTests.getNormalRange();
        dto.measurementUnit = labTests.getMeasurementUnit();
        return dto;
    }
}
