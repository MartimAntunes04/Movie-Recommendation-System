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

    @GetMapping("/view")
    public ResponseEntity<?> viewHistoryPage(
        @RequestHeader(name = "Authorization", required = true) String authorization,
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

    @PostMapping("/add_remove")
    public ResponseEntity<?> add_removeHistory(
        @RequestHeader(name = "Authorization", required = true) String authorization,
        @RequestParam (required = true) long id 
        ) throws IOException, InterruptedException {
        //check if history item already exists
        String token = authorization.substring("Bearer ".length());
        long userId = authService.getUserIdFromToken(token);
        
        //yes -> remove it
        if(historyService.remove(id, userId))  return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Removed from history"
                ));

        //no -> add it
        Movie fetched = movieService.fetchOrCreateMovieById(id);
        historyService.add(fetched, userId);
        
        return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Added to history"
            ));

    }

    
    @GetMapping("/checkHistory")
    public ResponseEntity<?> checkHistory(
        @RequestHeader(name = "Authorization", required = true) String authorization,
        @RequestParam (required = true) long id 
        ) throws IOException, InterruptedException {
        //check if history item already exists
        String token = authorization.substring("Bearer ".length());
        long userId = authService.getUserIdFromToken(token);
        
        if(historyService.check(id, userId))  return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Movie in user's history"
                ));

        return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Not in user's history"
            ));

    }
}
