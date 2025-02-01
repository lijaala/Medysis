package Medysis.Project.Repository;

import Medysis.Project.Model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordsRepository  extends JpaRepository<MedicalRecord, Integer> {
    List<MedicalRecord> findByUserId(Integer userId);

}
