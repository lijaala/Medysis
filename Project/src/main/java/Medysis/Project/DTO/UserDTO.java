package Medysis.Project.DTO; // Important: Place DTOs in a separate package

import Medysis.Project.DTO.RoleDTO;

import java.time.LocalDateTime;

public class UserDTO {

        public Integer userID;
        public String name;
        public String email;
        public String phone;  // Changed to lowercase 'phone' for consistency (optional)
        public String address;
        public String gender;
        public Integer age;
        public String image;
        public RoleDTO role; // Use RoleDTO (essential for breaking circular reference)
        public boolean verified;
        public LocalDateTime created_at;
        public LocalDateTime updated_at;



}