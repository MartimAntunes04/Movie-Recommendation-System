package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.springframework.stereotype.Service;
import pt.uc.movierecommendation.movierecommendationsystem.Model.SignUpRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    public String login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> encoder.matches(password, user.getPassword())) // compare raw vs hashed
                .map(user -> jwtService.generateToken(email))
                .orElse(null); // retorna null se login falhar
    }

    public boolean signup(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail()) ||
                userRepository.existsByUsername(signUpRequest.getUsername())) {
            return false;
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setUsername(signUpRequest.getUsername());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setRegistrationDate(LocalDate.now());
        userRepository.save(user);

        return true;
    }
}