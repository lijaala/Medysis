package Medysis.Project.Repository;

import Medysis.Project.Model.Role;
import Medysis.Project.Model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByStaffEmail(String staffEmail);

    boolean existsByStaffID(String staffID);
    List<Staff> findByRole(Role role);
    List<Staff> findByRoleRoleID(Integer roleID);

    Optional<Staff> findByResetToken(String token);

    @Query("SELECT s FROM Staff s WHERE s.staffID = :staffId AND s.deleted = false")
    Optional<Staff> findActiveById(@Param("staffId") String staffId);

    // Method to find all staff members where deleted is false
    @Query("SELECT s FROM Staff s WHERE s.deleted = false")
    List<Staff> findAllActiveStaff();
}

