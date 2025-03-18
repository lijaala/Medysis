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
    public List<MedicalRecord> getMedicalRecordsByUserId(@RequestParam (value = "userId", required = false)  Integer userID,HttpSession session) {
        if (userID == null) {
            Object userIdFromSession = session.getAttribute("userId");
            if (userIdFromSession instanceof String) {
                try {
                    userID = Integer.parseInt((String) userIdFromSession);
                } catch (NumberFormatException e) {
                    // Handle the case where the String in the session is not a valid integer
                    // Log the error and potentially return an error response
                    System.err.println("Error: userId in session is not a valid integer: " + userIdFromSession);
                    return Collections.emptyList(); // Or throw an appropriate exception
                }
            } else if (userIdFromSession instanceof Integer) {
                userID = (Integer) userIdFromSession;
            } else {
                // Handle the case where userId is not in the session or is of an unexpected type
                return Collections.emptyList(); // Or throw an appropriate exception
            }

            if (userID == null) {
                // Still null after trying to retrieve from session
                return Collections.emptyList(); // Or throw an appropriate exception
            }
        }
        return recordService.getMedicalRecordsByUserId(userID);
    }

    @PostMapping("/updateStatus")
    public String updateTreatmentStatus(@RequestBody List<MedicalRecord> records) {
        recordService.updateTreatmentStatus(records);
        return "Treatment status updated successfully!";
    }


}
