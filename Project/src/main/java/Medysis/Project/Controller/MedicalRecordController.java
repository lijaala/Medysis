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
import java.util.List;

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
                                @RequestParam Integer appointmentID,
                                @RequestParam Integer followUpMonths, // New field for follow-up
                                @RequestParam String treatmentPlan, // Treatment plan field
                                HttpSession session) {

        // Retrieve doctorID from the session
        String doctorID = (String) session.getAttribute("userId");

        if (doctorID == null) {
            return "Doctor ID is not found in session.";
        }

        // Call the service to add the diagnosis and update the appointment
        return recordService.addDiagnosis(userID, doctorID, conditionName, treatmentPlan, appointmentID, followUpMonths);
    }


    @GetMapping("/getByUserId")
    public List<MedicalRecord> getMedicalRecordsByUserId(@RequestParam Integer userID) {
        return recordService.getMedicalRecordsByUserId(userID);
    }

    @PostMapping("/updateStatus")
    public String updateTreatmentStatus(@RequestBody List<MedicalRecord> records) {
        recordService.updateTreatmentStatus(records);
        return "Treatment status updated successfully!";
    }


}
