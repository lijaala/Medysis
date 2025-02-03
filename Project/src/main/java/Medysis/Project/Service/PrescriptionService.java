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

        // Step 3: Fetch the user entity by userId (from the frontend)
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
        logger.info("Saved Prescription ID: " + savedPrescription.getPrescriptionID());

        // Step 7: Link prescribed medications to the saved prescription and save the prescribed medication records
        for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
            prescribedMedication.setPrescription(savedPrescription);
            logger.info("Saving Prescribed Medication ID: " + prescribedMedication.getMedication().getMedicationID());
            prescribedMedicationsRepository.save(prescribedMedication);
        }
    }
}
