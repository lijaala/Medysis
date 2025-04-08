package Medysis.Project.Repository;

import Medysis.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail( @Param("email")  String email);

    @Query("SELECT u FROM User u WHERE u.verificationCode = :verificationCode AND u.deleted = false")
    User findByVerificationCode(@Param("verificationCode")  String verificationCode);
    long count();


    // Count new patients in the last month
    @Query("SELECT COUNT(u) FROM User u WHERE u.created_at > :lastMonth AND u.deleted = false ")
    int countNewPatientsLastMonth(@Param("lastMonth") LocalDateTime lastMonth);

    @Query("SELECT u FROM User u WHERE u.resetToken = :token AND u.deleted = false")
    Optional<User> findByResetToken(String token);

    @Override
    @Query("SELECT u FROM User u WHERE u.userID = :id AND u.deleted = false")
    Optional<User> findById(@Param("id") Integer id);

    @Override
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAll();

    // Method to find a user by ID where deleted is false
    @Query("SELECT u FROM User u WHERE u.userID = :id AND u.deleted = false")
    Optional<User> findActiveById(@Param("id") Integer id);

    // Method to find all users where deleted is false
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActiveUsers();
}

