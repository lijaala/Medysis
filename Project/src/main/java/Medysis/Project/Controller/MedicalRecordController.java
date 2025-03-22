package Medysis.Project.Controller;


import Medysis.Project.Model.MedicalRecord;
import Medysis.Project.Repository.MedicalRecordsRepository;
import Medysis.Project.Service.RecordService;
import Medysis.Project.Service.UploadImageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
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
        String userid=(String)session.getAttribute("userId");
        Integer userID= Integer.parseInt(userid);
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
                                @RequestParam Integer followUpMonths,
                                @RequestParam String treatmentPlan,
                                @RequestParam(required = false) Integer recordId, // Add recordId parameter
                                HttpSession session) {

        String doctorID = (String) session.getAttribute("userId");
        if (doctorID == null) {
            return "Doctor ID is not found in session.";
        }

        if (recordId != null) {
            // Update existing record
            return recordService.updateDiagnosis(recordId, userID, doctorID, conditionName, treatmentPlan, appointmentID, followUpMonths);
        } else {
            // Create new record
            return recordService.addDiagnosis(userID, doctorID, conditionName, treatmentPlan, appointmentID, followUpMonths);
        }
    }


    @GetMapping("/getByUserId")
    public List<MedicalRecord> getMedicalRecordsByUserId(@RequestParam(value = "userId", required = false) Integer userID, HttpSession session) {
        if (userID != null) {
            // userID parameter is provided, use it
            return recordService.getMedicalRecordsByUserId(userID);
        }

        // userID parameter is NOT provided, use session userId
        Object userIdFromSession = session.getAttribute("userId");
        if (userIdFromSession instanceof Integer) {
            return recordService.getMedicalRecordsByUserId((Integer) userIdFromSession);
        } else {
            try {
                return recordService.getMedicalRecordsByUserId(Integer.parseInt((String) userIdFromSession));
            } catch (NumberFormatException e) {
                System.err.println("Error: userId in session is not a valid integer: " + userIdFromSession);
                return Collections.emptyList();
            }
        }
    }

    @PostMapping("/updateStatus")
    public String updateTreatmentStatus(@RequestBody List<MedicalRecord> records) {
        recordService.updateTreatmentStatus(records);
        return "Treatment status updated successfully!";
    }
    @GetMapping("/getByAppointmentId/{appointmentId}")
    public MedicalRecord getDiagnosisByAppointmentId(@PathVariable Integer appointmentId) {
        MedicalRecord record = recordService.getMedicalRecordByAppointmentId(appointmentId);
        if (record == null) {
            return new MedicalRecord(); // Return empty MedicalRecord object
        }
        return record;
    }


}
