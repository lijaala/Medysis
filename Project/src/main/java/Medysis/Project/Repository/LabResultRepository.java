package Medysis.Project.Repository;

import Medysis.Project.Model.LabOrder;
import Medysis.Project.Model.LabResults;
import Medysis.Project.Model.LabTests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Repository
public interface LabResultRepository extends JpaRepository<LabResults, Integer> {
    Optional<LabResults> findByOrderIDAndTestID(LabOrder labOrder, LabTests labTest);
   List<LabResults> findByOrderID(LabOrder labOrder);


}
