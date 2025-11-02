package pt.uc.movierecommendation.movierecommendationsystem.Utils;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

@Component
@Profile("dev") // only runs when SPRING_PROFILES_ACTIVE=dev
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed("luis123","luis@gmail.com","123456","Luis","Silva");
        seed("ana2025","ana@gmail.com","1234","Ana","Costa");
        seed("joaop","joao@gmail.com","123","João","Pereira");
    }

    private void seed(String username, String email, String rawPassword, String firstName, String lastName) {
        if (users.existsByEmail(email)) return;
        User u = new User();
        u.setUsername(username);                  // maps to user_name
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword)); // maps to hashed_password
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRegistrationDate(LocalDate.now());
        users.save(u);
    }
}
