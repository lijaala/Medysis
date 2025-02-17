package Medysis.Project.Repository;

import Medysis.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    User findByVerificationCode(String verificationCode);
    long count();

    // Count new patients in the last month
    @Query("SELECT COUNT(u) FROM User u WHERE u.created_at > :lastMonth")
    int countNewPatientsLastMonth(@Param("lastMonth") LocalDateTime lastMonth);
}

