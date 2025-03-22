package Medysis.Project.Service;

import Medysis.Project.DTO.MedicationDTO;
import Medysis.Project.DTO.PrescriptionResponse;
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
import java.util.Optional;
import java.util.stream.Collectors;

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
            String alternative = prescribedMedication.getMedication().getAlternative(); // Get alternative value

            if (medicationName == null || medicationName.isEmpty()) { // Check for empty medication name
                throw new IllegalArgumentException("Medication name cannot be empty.");
            }
            Medication medication = medicationRepository.findByMedicationName(medicationName).orElse(null);
            if (medication == null) {
                medication = new Medication();


                medication.setMedicationName(medicationName);
                medication.setAlternative(prescribedMedication.getMedication().getAlternative());
                medicationRepository.saveAndFlush(medication); // Save immediately to get the ID



            }

            prescribedMedication.setMedication(medication); // Set the medication (existing or new)
            medicationsToSave.add(medication);
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
    @Transactional
    public void addOrUpdatePrescription(Integer appointmentId, Prescription prescription, String staffId, Integer userId) {
        Optional<Prescription> existingPrescription = prescriptionRepository.findByAppointmentAppointmentID(appointmentId);

        if (existingPrescription.isPresent()) {
            updatePrescription(existingPrescription.get().getPrescriptionID(), prescription, staffId, userId);
        } else {
            addPrescription(appointmentId, prescription, staffId, userId);
        }
    }
    @Transactional
    public void updatePrescription(Long prescriptionId, Prescription prescription, String staffId, Integer userId) {
        Optional<Prescription> existingPrescriptionOptional = prescriptionRepository.findById(prescriptionId.intValue());

        if (existingPrescriptionOptional.isPresent()) {
            Prescription existingPrescription = existingPrescriptionOptional.get();

            logger.info("Deleting existing PrescribedMedications...");
            prescribedMedicationsRepository.deleteInBatch(existingPrescription.getPrescribedMedications());
            logger.info("Deleted existing PrescribedMedications.");
            // Clear existing prescribed medications



            // Update prescription fields
            existingPrescription.setStaff(staffRepository.findById(staffId).orElse(null));
            existingPrescription.setUser(userRepository.findById(userId).orElse(null));
            existingPrescription.setAppointment(appointmentRepository.findById(prescription.getAppointment().getAppointmentID()).orElse(null));
            existingPrescription.setPrescriptionDate(LocalDate.now());

            // Save the updated prescription
            Prescription updatedPrescription = prescriptionRepository.save(existingPrescription);

            // Save new prescribed medications
            for (PrescribedMedications prescribedMedication : prescription.getPrescribedMedications()) {
                String medicationName = prescribedMedication.getMedication().getMedicationName();
                Medication medication = medicationRepository.findByMedicationName(medicationName).orElse(null);

                if (medication == null) {
                    medication = new Medication();
                    medication.setMedicationName(medicationName);
                    medication.setAlternative(prescribedMedication.getMedication().getAlternative());
                    medicationRepository.save(medication);
                }

                prescribedMedication.setMedication(medication);
                prescribedMedication.setPrescription(updatedPrescription);
                prescribedMedicationsRepository.save(prescribedMedication);
            }
        } else {
            throw new RuntimeException("Prescription not found with ID: " + prescriptionId);
        }
    }
    public PrescriptionResponse getPrescriptionByAppointmentId(Integer appointmentId) {
        Optional<Prescription> prescription=prescriptionRepository.findByAppointmentAppointmentID(appointmentId);
        return prescription.map(p -> new PrescriptionResponse(
                p.getUser().getName(),
                p.getStaff().getStaffName(),
                p.getPrescriptionDate(),
                p.getPrescribedMedications().stream()
                        .map(med -> new MedicationDTO(med.getMedication().getMedicationName(), med.getDosage(), med.getIntake(), med.getMedicationInterval(), med.getDaysOfIntake()))
                        .collect(Collectors.toList())
        )).orElse(null);
    }

    public List<PrescriptionResponse> getPrescriptionsByUserId(Integer userId) {
        List<Prescription> prescriptions = prescriptionRepository.findByUserId(userId);
        return prescriptions.stream().map(p -> new PrescriptionResponse(
                p.getUser().getName(),
                p.getStaff().getStaffName(),
                p.getPrescriptionDate(),

                p.getPrescribedMedications().stream()
                        .map(med -> new MedicationDTO(med.getMedication().getMedicationName(), med.getDosage(), med.getIntake(), med.getMedicationInterval(),med.getDaysOfIntake()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());
    }
}