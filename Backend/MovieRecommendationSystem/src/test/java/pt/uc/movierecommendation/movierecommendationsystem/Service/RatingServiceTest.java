package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.RatingsRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class RatingServiceTest {

    private RatingsRepository ratingsRepository;
    private UserRepository userRepository;
    private MovieService movieService;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingsRepository = mock(RatingsRepository.class);
        userRepository = mock(UserRepository.class);
        movieService = mock(MovieService.class);
        ratingService = new RatingService(ratingsRepository, userRepository);
    }


    // ------------------- check -------------------
    @Test
    void check_returnsRating_whenExists() {
        Ratings rating = new Ratings();
        rating.setRating(4.5f);

        when(ratingsRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.of(rating));

        Float result = ratingService.check(100L, 42L);

        assertEquals(null,4.5f, result);
    }

    @Test
    void check_returnsNull_whenNotExists() {
        when(ratingsRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.empty());

        Float result = ratingService.check(100L, 42L);

        assertNull(result);
    }

    // ------------------- add -------------------
    @Test
    void add_createsRating_success() {
        User user = new User();
        user.setId(42L);

        Movie movie = new Movie();
        movie.setId(100L);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        Ratings savedRating = new Ratings();
        savedRating.setRating(5.0f);
        savedRating.setUser(user);
        savedRating.setMovie(movie);

        when(ratingsRepository.save(any(Ratings.class))).thenReturn(savedRating);

        Ratings result = ratingService.add(movie, 42L, 5.0f);

        assertEquals(null,5.0f, result.getRating());
        assertEquals(null, user, result.getUser());
        assertEquals(null, movie, result.getMovie());
        verify(ratingsRepository).save(any(Ratings.class));
    }

    @Test
    void add_throwsException_whenUserNotFound() {
        Movie movie = new Movie();
        movie.setId(100L);

        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ratingService.add(movie, 42L, 5.0f));
    }

    // ------------------- update -------------------
    @Test
    void update_returnsTrue_whenRatingExists() {
        Ratings existing = new Ratings();
        existing.setRating(3.0f);

        when(ratingsRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.of(existing));

        boolean result = ratingService.update(100L, 42L, 4.5f);

        assertTrue(result);
        assertEquals(null,4.5f, existing.getRating());
    }

    @Test
    void update_returnsFalse_whenRatingNotExists() {
        when(ratingsRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.empty());

        boolean result = ratingService.update(100L, 42L, 4.5f);

        assertFalse(result);
    }

}
