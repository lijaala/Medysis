package Medysis.Project.Controller;


import Medysis.Project.Model.LabTests;
import Medysis.Project.Service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
