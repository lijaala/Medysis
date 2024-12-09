package Medysis.Project.Repository;

import Medysis.Project.Model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface AvailabilityRepository  extends JpaRepository<Availability, Integer> {
}
