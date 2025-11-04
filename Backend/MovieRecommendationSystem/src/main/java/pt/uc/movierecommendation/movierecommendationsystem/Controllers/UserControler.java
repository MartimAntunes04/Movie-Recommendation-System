package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Service.ProfileService;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/profile")
public class UserControler {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getProfile() {
        // Security filter already validated the JWT and set Authentication
        return profileService.getProfile();
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User user) {
        boolean updated = profileService.updateProfile(
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),  // will be encoded in service
                user.getFirstName(),
                user.getLastName()
        );

        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Could not update profile"));
        }

        return ResponseEntity.ok(Map.of("success", true));
    }





}
