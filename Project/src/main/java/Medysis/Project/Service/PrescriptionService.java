package Medysis.Project.Service;

import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // Step 1: Fetch the Appointment entity by appointmentId
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found for ID: " + appointmentId));
        logger.info("Fetched Appointment ID: " + appointmentId);

        // Step 2: Fetch the staff entity by staffId
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));
        logger.info("Fetched Staff ID: " + staffId);

        // Step 3: Fetch the user entity by userID (from the prescription object)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));
        logger.info("Fetched User ID: " + userId);


        // Step 4: Save medications first
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            // Check if the medication exists, otherwise create and save a new one
            Medication existingMedication = medicationRepository.findByMedicationName(prescribedMedication.getMedication().getMedicationName())
                    .orElseGet(() -> {
                        Medication newMedication = new Medication();
                        newMedication.setMedicationName(prescribedMedication.getMedication().getMedicationName());
                        newMedication.setAlternative(prescribedMedication.getMedication().getAlternative());
                        return medicationRepository.save(newMedication);
                    });

            // Set the medication in the prescribed medication record
            prescribedMedication.setMedication(existingMedication);
            logger.info("Saved Medication ID: " + existingMedication.getMedicationID());
        }

        // Step 5: Set the staff and appointment in the prescription
        prescription.setStaff(staff);
        prescription.setAppointment(appointment);
        prescription.setUser(user);  // Associate the user with the prescription
        logger.info("Prescription details: " + prescription);

        // Step 6: Save the Prescription entity
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        prescriptionRepository.save(prescription);
        prescriptionRepository.flush(); // Ensure it's immediately persisted



        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            Medication medication = medicationRepository.findByMedicationName(prescribedMedication.getMedication().getMedicationName())
                    .orElseGet(() -> {
                        Medication newMedication = new Medication();
                        newMedication.setMedicationName(prescribedMedication.getMedication().getMedicationName());
                        newMedication.setAlternative(prescribedMedication.getMedication().getAlternative());

                        return medicationRepository.save(newMedication);
                    });

            // *** KEY CHANGES HERE ***
            prescribedMedication.setMedication(medication); // Set the actual Medication object!
            prescribedMedication.setPrescription(savedPrescription); // Link to the saved prescription


            // *** VALIDATION AND SETTING VALUES ***
            if (prescribedMedication.getDosage() == null || prescribedMedication.getDosage().isEmpty()) {
                throw new IllegalArgumentException("Dosage is required"); // Or handle differently
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
            // No need to re-set if the values are already there in the request body.

            prescribedMedicationsRepository.save(prescribedMedication);
        }


    }
}
