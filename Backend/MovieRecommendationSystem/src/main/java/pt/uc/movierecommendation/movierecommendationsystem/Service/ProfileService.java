package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ProfileService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> getProfile(String tokenHeader) {
        // remove o prefixo "Bearer "
        String token = tokenHeader.replace("Bearer ", "");

        String email = jwtService.extractEmail(token);

        if (email == null || !jwtService.isTokenValid(token, email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido ou expirado"));
        }

        //get user
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "email", user.getEmail(),
                        "username", user.getUsername(),
                        "password", user.getPassword(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilizador não encontrado")));

    }

    public boolean updateProfile(String tokenHeader, String newEmail, String newUsername, String newPassword, String newFirstName, String newLastName) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        if (email == null || !jwtService.isTokenValid(token, email)) {
            return false; // token inválido
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(newEmail != null ? newEmail : user.getEmail());
            user.setUsername(newUsername != null ? newUsername : user.getUsername());
            user.setPassword(newPassword != null ? newPassword : user.getPassword());
            user.setFirstName(newFirstName != null ? newFirstName : user.getFirstName());
            user.setLastName(newLastName != null ? newLastName : user.getLastName());

            userRepository.save(user);
            return true; // atualização bem-sucedida
        }

        return false; // usuário não encontrado

    }


}
