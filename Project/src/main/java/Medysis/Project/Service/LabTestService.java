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

    public LabTests updateLabTest(Integer id, LabTests updatedTest) {
        return labTestRepository.findById(id).map(test -> {
            test.setTestName(updatedTest.getTestName());
            test.setMeasurementUnit(updatedTest.getMeasurementUnit());
            test.setNormalRange(updatedTest.getNormalRange());
            return labTestRepository.save(test);
        }).orElseThrow(() -> new RuntimeException("Test not found with id: " + id));
    }

    public void deleteLabTest(Integer id) {
        if (!labTestRepository.existsById(id)) {
            throw new RuntimeException("Test not found with id: " + id);
        }
        labTestRepository.deleteById(id);
    }

    public LabTests addLabTest(String testName, String measurementUnit, String normalRange) {
        LabTests labTest = new LabTests();
        labTest.setTestName(testName);
        labTest.setMeasurementUnit(measurementUnit);
        labTest.setNormalRange(normalRange);
        return labTestRepository.save(labTest);
    }
}
