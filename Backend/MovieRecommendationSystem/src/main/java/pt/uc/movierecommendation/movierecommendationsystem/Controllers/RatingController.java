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

    public RatingController(RatingService ratingService, AuthService authService, 
        ProfileService profileService, MovieService movieService) {
        this.ratingService = ratingService;
        this.authService = authService;
        this.movieService = movieService;
    }


    @PostMapping("/uptade")
    public ResponseEntity<?> updateRating(
        @RequestHeader(name = "Authorization", required = true) String authorization,
        @RequestParam (required = true) long id,
        @RequestParam (required = true) float rating
        ) throws IOException, InterruptedException {
        //check if history item already exists
        String token = authorization.substring("Bearer ".length());
        long userId = authService.getUserIdFromToken(token);
        
        //yes -> update it
        if(ratingService.update(id, userId, rating))  return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "updated rating"
                ));

        //no -> add it
        Movie fetched = movieService.fetchOrCreateMovieById(id);
        ratingService.add(fetched, userId, rating);
        
        return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Added to history"
            ));

    }

    
    @GetMapping("/checkRating")
    public ResponseEntity<?> checkHistory(
        @RequestHeader(name = "Authorization", required = true) String authorization,
        @RequestParam (required = true) long id 
        ) throws IOException, InterruptedException {
        //check rating item already exists
        String token = authorization.substring("Bearer ".length());
        long userId = authService.getUserIdFromToken(token);
        
        Float rating = ratingService.check(id, userId);
        if(rating > 0.0f) return ResponseEntity.ok(Map.of(
                        "success", true,
                        "rating", rating
                ));

        return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "no rating"
            ));

    }
}
