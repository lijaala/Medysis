package Medysis.Project.Controller;


import Medysis.Project.Model.LabTests;
import Medysis.Project.Service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            LabTests savedLabTest = labTestService.addLabTest(testName, measurementUnit, normalRange);

            return ResponseEntity.ok(savedLabTest); // Or a different response
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding lab test");
        }
    }

}
