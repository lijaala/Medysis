package Medysis.Project.Repository;

import Medysis.Project.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository  extends JpaRepository<Role, Integer> {
    Integer roleID(int roleID);
    Role findByRole(String role);
}
