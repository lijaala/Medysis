package Medysis.Project.Service;

import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescribedMedicationRepository prescribedMedicationRepository;
    private final MedicationRepository medicationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    // ✅ Constructor Injection to initialize repositories
    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            PrescribedMedicationRepository prescribedMedicationRepository,
            MedicationRepository medicationRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            StaffRepository staffRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescribedMedicationRepository = prescribedMedicationRepository;
        this.medicationRepository = medicationRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
    }

    public Prescription addPrescription(Integer appointmentId, List<PrescribedMedications> prescribedMedications) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User user = appointment.getPatientID();
        Staff doctor = appointment.getDoctorID();

        // Check if a prescription already exists
        Prescription prescription = prescriptionRepository.findByAppointment_AppointmentID(appointmentId)
                .orElseGet(() -> {
                    Prescription newPrescription = new Prescription();
                    newPrescription.setUser(user);
                    newPrescription.setStaff(doctor);
                    newPrescription.setAppointment(appointment);
                    newPrescription.setPrescriptionDate(LocalDate.now());
                    return prescriptionRepository.save(newPrescription);
                });

        // Assign prescription to prescribed medications and save
        for (PrescribedMedications prescribedMedication : prescribedMedications) {
            prescribedMedication.setPrescription(prescription);

            // Fetch the actual medication entity using medicationID
            Medication medication = medicationRepository.findById(prescribedMedication.getMedication().getMedicationID())
                    .orElseThrow(() -> new RuntimeException("Medication not found"));

            prescribedMedication.setMedication(medication);
        }

        prescribedMedicationRepository.saveAll(prescribedMedications);
        return prescription;
    }
}



