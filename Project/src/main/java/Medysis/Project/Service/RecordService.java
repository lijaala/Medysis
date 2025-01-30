package Medysis.Project.Service;

import Medysis.Project.Model.MedicalRecord;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.MedicalRecordsRepository;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
public class RecordService {
    @Autowired
    private MedicalRecordsRepository medicalRecordsRepository;
    @Autowired
    private UploadImageService uploadImageService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StaffRepository staffRepository;

    public String addMedicalHistory(Integer userID, String conditionName, LocalDate diagnosedDate, String isTreated,MultipartFile[] scans){
        try{
            User user=userRepository.findById(userID).orElse(null);
            if(user==null){
                return "User Not Found";
            }

            String filePaths=saveUploadedFiles(scans);

            MedicalRecord history = new MedicalRecord();
            history.setUser(user);
            history.setDoctor(null);
            history.setConditionName(conditionName);
            history.setDiagnosedDate(diagnosedDate);
            history.setIsTreated(isTreated);
            history.setScans(filePaths);
            medicalRecordsRepository.save(history);
            return "Success";

        }
        catch(Exception e){
            e.printStackTrace();
            return "Error";
        }
    }
    private String saveUploadedFiles(MultipartFile[] files){
        StringBuilder pathBuilder= new StringBuilder();

        if(files!=null && files.length>0){
            for (MultipartFile file : files) {
                String savedFileName=uploadImageService.saveImage(file);
                if (savedFileName != null) {
                    pathBuilder.append(savedFileName).append(";");
                }
            }
        }
        return pathBuilder.toString();
    }
    public String addDaignosis(Integer userID, String staffID, String coondiitonName, LocalDate diagnosedDate,String isTreated, MultipartFile[] scans ){
        try{
            User user=userRepository.findById(userID).orElse(null);
            if(user==null){
                return "User Not Found";
            }
            Staff doctor=staffRepository.findById(staffID).orElse(null);
            if(doctor==null){
                return "Doctor Not Found";
            }
            String filePaths=saveUploadedFiles(scans);
            MedicalRecord history = new MedicalRecord();
            history.setUser(user);
            history.setDoctor(doctor);
            history.setConditionName(coondiitonName);
            history.setDiagnosedDate(diagnosedDate);
            history.setIsTreated(isTreated);
            history.setScans(filePaths);
            medicalRecordsRepository.save(history);
            return "Success";
        }
        catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

}

