package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Service.RatingService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.ProfileService;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/rating")
public class RatingController {

    @Value("${tmdb.api.key}")
    private String apiKey;


    private final RatingService ratingService;
    private final AuthService authService;
    private final MovieService movieService;

    public RatingController(
            RatingService ratingService,
            AuthService authService,
            MovieService movieService
    ) {
        this.ratingService = ratingService;
        this.authService = authService;
        this.movieService = movieService;
    }


    @PutMapping("/{movieId}")
    public ResponseEntity<?> updateRating(
            @RequestHeader (name = "Authorization", required = false) String authorization,
            @PathVariable long movieId,
            @RequestParam float rating
        ) throws IOException, InterruptedException {
        //check if history item already exists
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());

        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");


        try{
            // Try to update
            boolean updated = ratingService.update(movieId, userId, rating);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Rating updated"
                ));
            }

            // Else create new rating
            Movie movie = movieService.fetchOrCreateMovieById(movieId);
            ratingService.add(movie, userId, rating);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Rating added"
            ));
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Movie not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }


    }

    
    @GetMapping("/{movieId}")
    public ResponseEntity<?> checkRating(
            @RequestHeader (name = "Authorization", required = false) String authorization,
            @PathVariable long movieId
        ) {
        //check rating item already exists
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());

        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try{
            Float rating = ratingService.check(movieId, userId);
            if (rating != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "rating", rating
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No rating for this movie"
            ));
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Movie not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }


    }
}
