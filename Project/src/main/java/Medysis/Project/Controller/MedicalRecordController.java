package Medysis.Project.Controller;


import Medysis.Project.Model.MedicalRecord;
import Medysis.Project.Repository.MedicalRecordsRepository;
import Medysis.Project.Service.RecordService;
import Medysis.Project.Service.UploadImageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/medicalRecords")
public class MedicalRecordController {
    @Autowired
    private MedicalRecordsRepository medicalRecordsRepository;

    @Autowired
    private UploadImageService uploadImageService;

    @Autowired
    private RecordService recordService;



    @PostMapping("/history")
    public String addMedicalRecord(@RequestParam String conditionName, @RequestParam String isTreated, @RequestParam String diagnosedDate, @RequestParam MultipartFile[] scans, HttpSession session){
        Integer userID=(Integer)session.getAttribute("userId");
        if (userID==null){
            return "redirect:/login";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date=null;
        try{
            date=LocalDate.parse(diagnosedDate, formatter);

        }
        catch(DateTimeParseException e)
        {   e.printStackTrace();
            return "redirect:/login";
        }
         return recordService.addMedicalHistory(userID,conditionName,date, isTreated, scans);

    }
    @PostMapping("/saveDiagnosis")
    public String saveDiagnosis(@RequestParam Integer userID,
                                @RequestParam String conditionName,
                                @RequestParam String isTreated,
                                @RequestParam String diagnosedDate,
                                @RequestParam(required = false) MultipartFile[] scans,
                                @RequestParam Integer appointmentID,
                                HttpSession session) {

        String doctorID =(String) session.getAttribute("userId");  // Retrieve doctorID from the session

        if (doctorID == null) {
            return "Doctor ID is not found in session.";
        }

        // Parse the diagnosedDate string to a LocalDate object
        LocalDate date = LocalDate.parse(diagnosedDate);

        // Call the service to add the diagnosis
        return recordService.addDiagnosis(userID, doctorID, conditionName, date, isTreated, scans, appointmentID);
    }

}
