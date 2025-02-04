package Medysis.Project.Service;

import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private PrescribedMedicationRepository prescribedMedicationsRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addPrescription(Integer appointmentId, Prescription prescription, String staffId, Integer userId) {
        // 1. Fetch Required Entities (same as before)
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found for ID: " + appointmentId));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        // 2. Handle Medications (Crucial: Save Medications FIRST)
        List<Medication> medicationsToSave = new ArrayList<>();
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            String medicationName = prescribedMedication.getMedication().getMedicationName();
            Medication medication = medicationRepository.findByMedicationName(medicationName)
                    .orElseGet(() -> {
                        Medication newMedication = new Medication();
                        newMedication.setMedicationName(medicationName);
                        newMedication.setAlternative(prescribedMedication.getMedication().getAlternative());
                        medicationsToSave.add(newMedication);
                        return newMedication;
                    });
            prescribedMedication.setMedication(medication); // Set the medication (existing or new)
        }

        // Save ALL medications in one go *before* processing PrescribedMedications
        List<Medication> savedMedications = medicationRepository.saveAllAndFlush(medicationsToSave); // This is the fix!

        // 3. Save Prescription
        prescription.setStaff(staff);
        prescription.setAppointment(appointment);
        prescription.setUser(user);
        prescription.setPrescriptionDate(LocalDate.now());
        Prescription savedPrescription = prescriptionRepository.saveAndFlush(prescription);
        logger.info("Saved Prescription ID: " + savedPrescription.getPrescriptionID());


        // 4. Save PrescribedMedications (Link to saved Prescription and Medications)
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            prescribedMedication.setPrescription(savedPrescription); // Link to the saved prescription

            // Validation (same as before)
            String medicationName = prescribedMedication.getMedication().getMedicationName();
            if (prescribedMedication.getDosage() == null || prescribedMedication.getDosage().isEmpty()) {
                throw new IllegalArgumentException("Dosage is required for medication: " + medicationName);
            }
            if (prescribedMedication.getIntake() == null || prescribedMedication.getIntake().isEmpty()) {
                throw new IllegalArgumentException("Intake is required for medication: " + medicationName);
            }
            if (prescribedMedication.getMedicationInterval() == null || prescribedMedication.getMedicationInterval().isEmpty()) {
                throw new IllegalArgumentException("Frequency is required for medication: " + medicationName);
            }
            if (prescribedMedication.getDaysOfIntake() == 0) {
                throw new IllegalArgumentException("Days of intake is required for medication: " + medicationName);
            }

            prescribedMedicationsRepository.save(prescribedMedication);
            logger.info("Saved PrescribedMedication for Medication ID: " + prescribedMedication.getMedication().getMedicationID());
        }
    }
}