package Medysis.Project.Controller;


import Medysis.Project.Model.LabTests;
import Medysis.Project.Service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/labTests")
public class LabTestsController {
    @Autowired
    private LabTestService labTestService;

    @GetMapping("/availableTests")
    public List<LabTests> getAvailableTests() {
        return labTestService.getAllLabTests();
    }

    @PutMapping("/update/{id}")
    public LabTests updateLabTest(@PathVariable Integer id, @RequestBody LabTests updatedTest) {
        return labTestService.updateLabTest(id, updatedTest);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteLabTest(@PathVariable Integer id) {
        labTestService.deleteLabTest(id);
        return "Test deleted successfully";
    }

    @PostMapping("/add")
    public ResponseEntity<?> addLabTest(
            @RequestParam String testName,
            @RequestParam String measurementUnit,
            @RequestParam String normalRange) {

        try {
            LabTests labTest = new LabTests();
            labTest.setTestName(testName);
            labTest.setMeasurementUnit(measurementUnit);
            labTest.setNormalRange(normalRange);

            LabTests savedLabTest = labTestService.addLabTest(labTest);
            return ResponseEntity.ok(savedLabTest);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding lab test");
        }
    }
}
