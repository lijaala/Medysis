package Medysis.Project.Controller;


import Medysis.Project.Model.LabTests;
import Medysis.Project.Service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
