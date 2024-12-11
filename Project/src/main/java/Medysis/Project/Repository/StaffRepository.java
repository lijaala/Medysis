package Medysis.Project.Repository;

import Medysis.Project.Model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByStaffEmail(String staffEmail);

    boolean existsByStaffID(String staffID);

}

