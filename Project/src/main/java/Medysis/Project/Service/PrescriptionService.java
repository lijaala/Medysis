package Medysis.Project.Service;

import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        // Step 1: Fetch required entities
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found for ID: " + appointmentId));
        logger.info("Fetched Appointment ID: " + appointmentId);

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));
        logger.info("Fetched Staff ID: " + staffId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));
        logger.info("Fetched User ID: " + userId);

        // Step 2: Ensure medications exist before proceeding
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            Medication medication = medicationRepository.findByMedicationName(prescribedMedication.getMedication().getMedicationName())
                    .orElseGet(() -> {
                        Medication newMedication = new Medication();
                        newMedication.setMedicationName(prescribedMedication.getMedication().getMedicationName());
                        newMedication.setAlternative(prescribedMedication.getMedication().getAlternative());
                        return medicationRepository.save(newMedication); // Save before using it
                    });

            prescribedMedication.setMedication(medication); // Now the medication is fully saved
            logger.info("Saved Medication ID: " + medication.getMedicationID());
        }

        // Step 3: Save the Prescription entity
        prescription.setStaff(staff);
        prescription.setAppointment(appointment);
        prescription.setUser(user);
        prescription.setPrescriptionDate(LocalDate.now()); // Set the prescription date
        Prescription savedPrescription = prescriptionRepository.saveAndFlush(prescription); // Use saveAndFlush
        logger.info("Saved Prescription ID: " + savedPrescription.getPrescriptionID());


        // Step 4: Save prescribed medications linked to the prescription
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            prescribedMedication.setPrescription(savedPrescription); // Associate it with saved Prescription

            if (prescribedMedication.getDosage() == null || prescribedMedication.getDosage().isEmpty()) {
                throw new IllegalArgumentException("Dosage is required");
            }
            if (prescribedMedication.getIntake() == null || prescribedMedication.getIntake().isEmpty()) {
                throw new IllegalArgumentException("Intake is required");
            }
            if (prescribedMedication.getMedicationInterval() == null || prescribedMedication.getMedicationInterval().isEmpty()) {
                throw new IllegalArgumentException("Frequency is required");
            }
            if (prescribedMedication.getDaysOfIntake() == 0) {
                throw new IllegalArgumentException("Days of intake is required");
            }

            prescribedMedicationsRepository.save(prescribedMedication);
        }
    }

}
