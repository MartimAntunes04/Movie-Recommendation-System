package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uc.movierecommendation.movierecommendationsystem.Model.LoginRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Model.SignUpRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid email or password"));
        }
        return ResponseEntity.ok(Map.of("success", true, "token", token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup (@RequestBody SignUpRequest signUpRequest) {
        boolean success = authService.signup(signUpRequest);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully!"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "Email or username already in use!"
                    ));
        }
    }
}