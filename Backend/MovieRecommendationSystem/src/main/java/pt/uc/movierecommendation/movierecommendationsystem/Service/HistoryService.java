package pt.uc.movierecommendation.movierecommendationsystem.Service;

import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.MovieRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.GenreRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class HistoryService {

    
    private final HistoryItemRepository historyItemRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public HistoryService(HistoryItemRepository historyItemRepository, UserRepository userRepository, 
        GenreRepository genreRepository,  MovieRepository movieRepository) {

        this.historyItemRepository = historyItemRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : null;
    }

    public List<HistoryItem> getCurrentUserHistory() {
        String email = currentEmail();
        if (email == null) return Collections.emptyList();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return Collections.emptyList();

        Long userId = optionalUser.get().getId();
        return historyItemRepository.findByUser_IdOrderByWatchedDateDesc(userId);
    }

    public boolean moviePresent(long id) {
        if (movieRepository.findById(id).isPresent()) return true;
        return false;
    }

    @Transactional
    public Movie addMovie(Movie movie) {
        if (movie == null) throw new NullPointerException();
        return movieRepository.save(movie);
    }
}