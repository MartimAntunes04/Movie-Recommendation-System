package pt.uc.movierecommendation.movierecommendationsystem.Service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.WatchListItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WatchlistService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WatchListItemRepository watchListItemRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;

    public WatchlistService(WatchListItemRepository watchListItemRepository, UserRepository userRepository, MovieService movieService) {
        this.watchListItemRepository = watchListItemRepository;
        this.userRepository = userRepository;
        this.movieService = movieService;
    }

    public boolean check(long movieId, long userId) {
       Optional<WatchListItem> item = watchListItemRepository.findByUser_IdAndMovie_Id(userId, movieId);
        if (item == null || item.isEmpty()) return false;
        return true;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> get(Long userId, int page, int size) throws IllegalArgumentException {
        // Verifying inputs
        if (userId == null || userId <= 0 || page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        // Ensure that user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Fetch paginated watchlist items
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "addedDate"));
        Page<WatchListItem> items = watchListItemRepository.findByUser_Id(userId, pageable);
        int totalPages = items.getTotalPages();

        // If requested page exceeds total pages, return last page
        if (page >= totalPages && totalPages > 0) {
            pageable = PageRequest.of(totalPages - 1, size, Sort.by(Sort.Direction.DESC, "addedDate"));
            items = watchListItemRepository.findByUser_Id(userId, pageable);
        }

        // Mapping WatchListItems to JSON format
        return items.map(item -> {
            var movie = item.getMovie();
            Map<String, Object> json = new HashMap<>();
            json.put("itemId", item.getId());
            json.put("addedDate", item.getAddedDate());
            json.put("movieId", movie.getId());
            json.put("title", movie.getTitle());
            return json;
        });
    }

    @Transactional
    public WatchListItem add(Long userId, Long movieId) throws IllegalArgumentException, IllegalStateException {
        // Verifying inputs
        if (userId == null || userId <= 0 || movieId == null || movieId <= 0) {
            throw new IllegalArgumentException("Invalid userId or movieId");
        }
        
        // Ensure that user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Fetch or create movie
        Movie movie;
        try {
            movie = movieService.fetchOrCreateMovieById(movieId);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("Failed to fetch movie data: " + e.getMessage());
        }
        
        // Check if movie already in watchlist
        if (watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movie.getId())) {
            throw new IllegalStateException("Movie already in watchlist");
        }

        // Create and save new watchlist item
        WatchListItem item = new WatchListItem();
        item.setUser(user);
        item.setMovie(movie);
        item.setAddedDate(LocalDate.now());
        return watchListItemRepository.save(item);
    }

    @Transactional
    public void remove(Long userId, Long movieId) throws IllegalArgumentException, IllegalStateException {
        // Verifying inputs
        if (userId == null || userId <= 0 || movieId == null || movieId <= 0) {
            throw new IllegalArgumentException("Invalid userId or movieId");
        }

        // Ensure that user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if movie is in watchlist
        if (!watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movieId)) {
            throw new IllegalStateException("Movie not in watchlist");
        }

        // Remove movie from watchlist
        watchListItemRepository.deleteByUser_IdAndMovie_Id(userId, movieId);
    }
}
