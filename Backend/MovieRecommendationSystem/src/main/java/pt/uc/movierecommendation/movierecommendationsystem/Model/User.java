package pt.uc.movierecommendation.movierecommendationsystem.Model;


import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", nullable = false, length = 512)
    private String username;

    @Column(nullable = false, length = 512)
    private String email;

    @Column(name = "hashed_password", nullable = false, length = 512)
    private String password;

    @Column(name = "first_name", nullable = false, length = 512)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 512)
    private String lastName;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName= firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
}
