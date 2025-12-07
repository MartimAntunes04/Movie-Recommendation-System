package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Service.HistoryService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.ProfileService;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Value("${tmdb.api.key}")
    private String apiKey;


    private final HistoryService historyService;
    private final AuthService authService;
    private final MovieService movieService;

    public HistoryController(HistoryService historyService, AuthService authService, 
        ProfileService profileService, MovieService movieService) {
        this.historyService = historyService;
        this.authService = authService;
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<?> viewHistoryPage(
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) throws IOException, InterruptedException {
        //get current user's id
        String token = authorization.substring("Bearer ".length());
         Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        // Get list of history items
        var historyList = historyService.getCurrentUserHistory(authUserId);
        

        if (historyList.isEmpty()) {
        // Return a message if no movies in history
            return ResponseEntity.ok(Map.of("message", "No movies in your history."));
        }

        int pagesMax = (historyList.size() + size - 1) / size;
        int startIndex = Math.min(page * size, (pagesMax-1)*size);
        int endIndex = Math.min((page + 1) * size, historyList.size());
        // Return JSON list to frontend
        return ResponseEntity.ok(Map.of(
            "historyList", historyList.subList(startIndex, endIndex), 
            "pages", ""+ page + "/"+ pagesMax));
    }

    @PostMapping("/{movieId}")
    public ResponseEntity <?> addMovietoHistory(
            @RequestHeader (name = "Authorization", required = false) String authorization,
            @PathVariable Long movieId
    ){
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());

        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try{
            Movie fetched = movieService.fetchOrCreateMovieById(movieId);
            historyService.add(fetched, authUserId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Added to history"
                    ));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity <?> deleteMovietoHistory(
            @RequestHeader (name = "Authorization", required = false) String authorization,
            @PathVariable Long movieId
    ){
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());

        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try{
            boolean removed = historyService.remove(movieId, authUserId);
            if (removed) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Removed from history"
                ));
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Movie not found in history"
                        ));
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }

    }


    @GetMapping("/checkHistory/{movieId}")
    public ResponseEntity<?> checkHistory(
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @PathVariable  long movieId
        ) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());

        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try{

            boolean inHistory = historyService.check(movieId, authUserId);
            if (inHistory) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Movie in user's history"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Movie not in user's history"
                ));
            }

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }

    }
}
