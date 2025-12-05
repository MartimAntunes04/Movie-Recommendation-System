package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Map;

import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.WatchlistService;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;
    private final AuthService authService;

    public WatchlistController(WatchlistService watchlistService, AuthService authService) {
        this.watchlistService = watchlistService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getWatchlist(
        @RequestHeader(name = "Authorization", required = true) String authorization,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String token = authorization.substring("Bearer ".length());

        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try {
            Page<Map<String, Object>> result = watchlistService.get(authUserId, page, size);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    

    @PostMapping("/{movieId}")
    public ResponseEntity<?> addMovieToWatchlist(
        @PathVariable Long movieId,
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String token = authorization.substring("Bearer ".length());

        // Ensure AuthService can extract user id from token
        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try {
            watchlistService.add(authUserId, movieId);
            return ResponseEntity.ok(
                    Map.of("success", true, "message", "Movie added to watchlist")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> removeMovieFromWatchlist(
        @PathVariable Long movieId,
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String token = authorization.substring("Bearer ".length());

        // Ensure AuthService can extract user id from token
        Long authUserId = authService.getUserIdFromToken(token);
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try {
            watchlistService.remove(authUserId, movieId);
            return ResponseEntity.ok(
                    Map.of("success", true, "message", "Movie removed from watchlist")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Movie not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}