package Medysis.Project.Repository;

import Medysis.Project.Model.LabTests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LabTestRepository extends JpaRepository<LabTests, Integer> {
}
