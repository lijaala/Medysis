package Medysis.Project.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Integer userID;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String Phone;

    @Column(name = "address")
    private String address;

    @Column (name = "gender")
    private String gender;

    @Column(name = "age")
    private Integer age;

    @Column (name = "image")
    private String image;

    @Column
    private String password;

    @ManyToOne
    @JoinColumn(name = "role" , referencedColumnName = "roleID")
    private Role role;

    @Column(name = "verificationCode", length = 64)
    private String verificationCode;

    private boolean verified=false;

    @Column
    private LocalDateTime created_at;

    @Column
    private LocalDateTime updated_at;

    public void setId(Integer id) {
        this.userID = userID;
    }

    public Integer getId() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public String getVerificationCode() {return verificationCode;}

    public void setVerificationCode(String verificationCode)
    {this.verificationCode = verificationCode;}

    public boolean isVerified() {return verified;}
    public void setVerified(boolean verified) {this.verified = verified;}


    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User(Integer userID) {
        this.userID = userID;
    }
    public User() {
        // This constructor is intentionally empty.  JPA needs it.
    }

}
