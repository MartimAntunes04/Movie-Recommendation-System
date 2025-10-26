package pt.uc.movierecommendation.movierecommendationsystem.Model;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Username;
    private String email;
    private String password;
    private String FirstName;
    private String LastName;


    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return FirstName; }
    public void setFirstName(String firstName) { FirstName = firstName; }

    public String getLastName() { return LastName; }
    public void setLastName(String lastName) { LastName = lastName; }

    public String getUsername() { return Username; }
    public void setUsername(String username) { Username = username; }
}
