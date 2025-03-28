package Medysis.Project.Service;

import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.MedicalRecord;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.AppointmentRepository;
import Medysis.Project.Repository.MedicalRecordsRepository;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RecordService {
    @Autowired
    private MedicalRecordsRepository medicalRecordsRepository;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

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
    public String addDiagnosis(Integer userID, String doctorID, String conditionName, String treatmentPlan, Integer appointmentID, Integer followUpMonths) {
        try {
            // Fetch user and doctor from the database
            User user = userRepository.findById(userID).orElse(null);
            if (user == null) {
                return "User Not Found";
            }

            Staff doctor = staffRepository.findById(doctorID).orElse(null);
            if (doctor == null) {
                return "Doctor Not Found";
            }

            // Fetch the appointment by appointmentID
            Appointment appointment = appointmentRepository.findById(appointmentID).orElse(null);
            if (appointment == null) {
                return "Appointment Not Found";
            }

            // Set the diagnosed date to today
            LocalDate diagnosedDate = LocalDate.now();

            // Set status to "Ongoing" as per your requirement
            String status = "Ongoing";

            // Set scans to null (no scans provided)
            String filePaths = null;

            // Create a new medical record with the provided details
            MedicalRecord history = new MedicalRecord();
            history.setUser(user); // Set the user (patient)
            history.setDoctor(doctor); // Set the doctor
            history.setConditionName(conditionName); // Set the condition name
            history.setDiagnosedDate(diagnosedDate); // Set the diagnosis date
            history.setIsTreated(status); // Set status to Ongoing
            history.setScans(filePaths); // No scans provided
            history.setTreatmentPlan(treatmentPlan); // Set treatment plan
            history.setAppointment(appointment); // Link the medical record to the appointment

            // Save the medical record in the database
            medicalRecordsRepository.save(history);

            // Calculate the follow-up date based on the appointment date
            LocalDate appointmentDate = appointment.getAppDate(); // Get the actual appointment date
            LocalDate followUpDate = null; // Add the follow-up months

            // Update the appointment's follow-up date
            if (followUpMonths != null) {
                System.out.println(followUpMonths);
                followUpDate = appointmentDate.plusMonths(followUpMonths);
                System.out.println(followUpDate);

                // Update the appointment's follow-up date and send notifications
                appointmentService.setFollowUpAppointment(appointmentID, followUpDate);
            } else {
                // If no followUpMonths are provided, ensure followUpDate is null in the appointment
                appointment.setFollowUpDate(null);
                appointmentRepository.save(appointment);
            }
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
            return "Diagnosis added successfully!";


        } catch (Exception e) {
            e.printStackTrace();
            return "Error";  // Handle any exceptions
        }
    }
    public List<MedicalRecord> getMedicalRecordsByUserId(Integer userID) {
        System.out.println("Service: Retrieving records for userID: " + userID);
        List<MedicalRecord> records = medicalRecordsRepository.findByUserId(userID); // Assuming your repository method is named findByUserID
        System.out.println("Service: Retrieved records: " + records);
        return records;
    }

    public void updateTreatmentStatus(List<MedicalRecord> records) {
        for (MedicalRecord record : records) {
            Optional<MedicalRecord> optionalRecord = medicalRecordsRepository.findById(record.getRecordID());
            optionalRecord.ifPresent(existingRecord -> {
                existingRecord.setIsTreated(record.getIsTreated()); // Only update status
                medicalRecordsRepository.save(existingRecord);
            });
        }
    }

    public MedicalRecord getMedicalRecordByAppointmentId(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null); // Find Appointment object
        if (appointment != null) {
            return medicalRecordsRepository.findByAppointment(appointment);
        }
        return null; // Or throw an exception if appointment is not found
    }

    public String updateDiagnosis(Integer recordId, Integer userID, String doctorID, String conditionName, String treatmentPlan, Integer appointmentID, Integer followUpMonths) {
        Optional<MedicalRecord> optionalRecord = medicalRecordsRepository.findById(recordId);
        User user = userRepository.findById(userID).orElse(null);
        Staff doctor = staffRepository.findById(doctorID).orElse(null);
        Appointment appointment = appointmentRepository.findById(appointmentID).orElse(null);

        if (optionalRecord.isPresent()) {
            MedicalRecord record = optionalRecord.get();

            // Update the record's fields
            record.setUser(user);
            record.setDoctor(doctor);
            record.setConditionName(conditionName);
            record.setTreatmentPlan(treatmentPlan);
            record.setAppointment(appointment);

            LocalDate followUpDate = null;

            if (followUpMonths != null) {
                followUpDate = appointment.getAppDate().plusMonths(followUpMonths);
                // Update the appointment's follow-up date and send notifications
                appointmentService.setFollowUpAppointment(appointmentID, followUpDate);
            } else {
                // If followUpMonths is null, ensure followUpDate is null in the appointment
                appointment.setFollowUpDate(null);
                appointmentRepository.save(appointment);
            }

            medicalRecordsRepository.save(record);
            return "Diagnosis updated successfully!";
        } else {
            return "Record not found!";
        }
    }
}




