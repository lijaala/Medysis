package Medysis.Project.Repository;

import Medysis.Project.Model.PrescribedMedications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescribedMedicationRepository extends JpaRepository<PrescribedMedications, Long> {
    List<PrescribedMedications> findByPrescription_PrescriptionID(Long prescriptionId);
}

