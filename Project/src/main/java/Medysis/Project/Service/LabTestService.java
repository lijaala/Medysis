package Medysis.Project.Service;

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
}
