package pt.uc.movierecommendation.movierecommendationsystem.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Genre;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;
import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.MovieRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.RatingsRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.WatchListItemRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private RatingsRepository ratingsRepository;
    private WatchListItemRepository watchListItemRepository;
    private MovieRepository movieRepository;
    private MovieService movieService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepository.class);
        ratingsRepository = mock(RatingsRepository.class);
        watchListItemRepository = mock(WatchListItemRepository.class);
        movieService = Mockito.spy(new MovieService(movieRepository, ratingsRepository, watchListItemRepository));

        try {
            // Injecting a fake API key
            var apiKeyField = MovieService.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(movieService, "fake-api-key");
        } catch (Exception ignored) {}
    }

    // ========================= TESTS RECOMMENDATIONS =========================

    @Test
    void getRecommendedGenresIds_shouldReturnEmpty_whenNoData() throws InterruptedException {
        Long userId = 1L;
        
        when(ratingsRepository.findTop20ByUser_IdAndRatingLessThanEqualOrderByRatingAsc(eq(userId), anyInt()))
                .thenReturn(new ArrayList<>());
        when(watchListItemRepository.findByUser_Id(userId))
                .thenReturn(new ArrayList<>());

        List<Integer> result = movieService.getRecommendedGenresIds(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRecommendedGenresIds_shouldCountGenres() throws InterruptedException {
        Long userId = 1L;

        // Setup Genres
        Genre action = createGenre(28L, "Action");
        Genre comedy = createGenre(35L, "Comedy");
        Genre drama = createGenre(18L, "Drama");

        // Setup Movies
        Movie movie1 = createMovie(101L, Set.of(action, comedy));
        Movie movie2 = createMovie(102L, Set.of(action));
        Movie movie3 = createMovie(103L, Set.of(drama));

        // Mock Ratings (movie1 and movie2)
        Ratings rating1 = new Ratings();
        Ratings rating2 = new Ratings();
        rating1.setMovie(movie1);
        rating2.setMovie(movie2);

        // Mock Watchlist (movie3)
        WatchListItem watchListItem1 = new WatchListItem();
        watchListItem1.setMovie(movie3);
        
        // Setting up mocks
        when(ratingsRepository.findTop20ByUser_IdAndRatingGreaterThanEqualOrderByRatingDesc(eq(userId), anyInt()))
                .thenReturn(List.of(rating1, rating2));
        when(watchListItemRepository.findByUser_Id(userId))
                .thenReturn(List.of(watchListItem1));
        
        List<Integer> result = movieService.getRecommendedGenresIds(userId);

        // Verifying results
        assertEquals(3, result.size());
        assertTrue(result.contains(28)); // Action
        assertTrue(result.contains(35)); // Comedy
        assertTrue(result.contains(18)); // Drama
        
        // Action should be first (count = 2)
        assertEquals(28, result.get(0));
    }

    @Test
    void getRecommendedGenresIds_shouldLimitToTop3() throws InterruptedException {
        Long userId = 1L;

        Genre g1 = createGenre(1L, "G1");
        Genre g2 = createGenre(2L, "G2");
        Genre g3 = createGenre(3L, "G3");
        Genre g4 = createGenre(4L, "G4");

        // Create a movie with 4 genres
        Movie movie1 = createMovie(100L, Set.of(g1, g2, g3, g4));

        Ratings rating1 = new Ratings();
        rating1.setMovie(movie1);

        when(ratingsRepository.findTop20ByUser_IdAndRatingGreaterThanEqualOrderByRatingDesc(eq(userId), anyInt()))
                .thenReturn(List.of(rating1));
        when(watchListItemRepository.findByUser_Id(userId)).thenReturn(new ArrayList<>());

        List<Integer> result = movieService.getRecommendedGenresIds(userId);

        // Verifying results
        assertEquals(3, result.size(), "Should be limited to top 3 genres");
    }

    @Test
    void getRecommendedGenresIds_shouldHandleNulls() throws InterruptedException {
        Long userId = 1L;

        // Rating with null movie
        Ratings rating1 = new Ratings(); 
        rating1.setMovie(null);

        // Rating with movie but null genres
        Movie movie2 = new Movie();
        Ratings rating2 = new Ratings();
        movie2.setGenres(null);
        rating2.setMovie(movie2);

        when(ratingsRepository.findTop20ByUser_IdAndRatingLessThanEqualOrderByRatingAsc(eq(userId), anyInt()))
                .thenReturn(List.of(rating1, rating2));
        when(watchListItemRepository.findByUser_Id(userId)).thenReturn(new ArrayList<>());

        List<Integer> result = movieService.getRecommendedGenresIds(userId);

        // Verifying results
        assertTrue(result.isEmpty());
        assertDoesNotThrow(() -> movieService.getRecommendedGenresIds(userId));
    }

    // ========================= TESTS MOVIE FETCH/CREATE =========================

    @Test
    void fetchOrCreateMovieById_shouldThrowOnInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> movieService.fetchOrCreateMovieById(null));
        assertThrows(IllegalArgumentException.class, () -> movieService.fetchOrCreateMovieById(0L));
        assertThrows(IllegalArgumentException.class, () -> movieService.fetchOrCreateMovieById(-5L));
    }

    @Test
    void fetchOrCreateMovieById_shouldReturnExisting_whenInDb() throws Exception {
        Long movieid = 123L;
        Movie existing = new Movie();
        existing.setId(movieid);
        existing.setTitle("Already Stored");

        when(movieRepository.findById(movieid)).thenReturn(Optional.of(existing));

        Movie result = movieService.fetchOrCreateMovieById(movieid);

        assertSame(existing, result);
        verify(movieRepository, never()).save(any());
        verify(movieService, never()).getMovieDetails(anyLong());
        verify(movieService, never()).getMovieCredits(anyLong());
    }

    @Test
    void fetchOrCreateMovieById_shouldCreateAndSave_whenNotInDb() throws Exception {
        Long movieid = 550L;
        when(movieRepository.findById(movieid)).thenReturn(Optional.empty());

        // Stub TMDb responses
        JsonNode details = mapper.readTree("{\"title\":\"Fight Club\",\"overview\":\"Desc\",\"release_date\":\"1999-10-15\",\"vote_average\":8.4}");
        JsonNode credits = mapper.readTree("{\"crew\":[{\"job\":\"Director\",\"name\":\"David Fincher\"}],\"cast\":[{\"name\":\"Brad Pitt\"},{\"name\":\"Edward Norton\"},{\"name\":\"Helena Bonham Carter\"},{\"name\":\"Meat Loaf\"},{\"name\":\"Jared Leto\"},{\"name\":\"Other\"}]}");

        doReturn(details).when(movieService).getMovieDetails(movieid);
        doReturn(credits).when(movieService).getMovieCredits(movieid);

        // Stub save to echo entity with ID
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0, Movie.class));

        Movie result = movieService.fetchOrCreateMovieById(movieid);

        assertNotNull(result);
        assertEquals(movieid, result.getId());
        assertEquals("Fight Club", result.getTitle());
        assertEquals("Desc", result.getDescription());
        assertEquals(LocalDate.of(1999, 10, 15), result.getReleaseDate());
        assertEquals(8.4, result.getAverageRating());
        assertEquals("David Fincher", result.getDirector());
        assertTrue(result.getCastMembers().contains("Brad Pitt"));
        assertTrue(result.getCastMembers().contains("Edward Norton"));
        // limited to top 5
        assertFalse(result.getCastMembers().contains("Other"));

        verify(movieRepository).save(any(Movie.class));
        verify(movieService).getMovieDetails(movieid);
        verify(movieService).getMovieCredits(movieid);
    }

    @Test
    void fetchOrCreateMovieById_shouldPropagateTmdbDetailsError() throws Exception {
        Long movieid = 999L;
        when(movieRepository.findById(movieid)).thenReturn(Optional.empty());

        doThrow(new IllegalArgumentException("TMDb details failed: 401"))
                .when(movieService).getMovieDetails(movieid);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> movieService.fetchOrCreateMovieById(movieid));
        assertTrue(ex.getMessage().contains("TMDb details failed"));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void fetchOrCreateMovieById_shouldPropagateTmdbCreditsError() throws Exception {
        Long movieid = 1000L;
        when(movieRepository.findById(movieid)).thenReturn(Optional.empty());

        JsonNode details = mapper.readTree("{\"title\":\"X\",\"overview\":null,\"release_date\":null,\"vote_average\":0}");
        doReturn(details).when(movieService).getMovieDetails(movieid);
        doThrow(new IllegalArgumentException("TMDb credits failed: 401"))
                .when(movieService).getMovieCredits(movieid);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> movieService.fetchOrCreateMovieById(movieid));
        assertTrue(ex.getMessage().contains("TMDb credits failed"));
        verify(movieRepository, never()).save(any());
    }

    // ========================= HELPERS =========================

    private Genre createGenre(Long id, String name) {
        Genre g = new Genre();
        g.setId(id);
        g.setName(name);
        return g;
    }

    private Movie createMovie(Long id, Set<Genre> genres) {
        Movie m = new Movie();
        m.setId(id);
        m.setGenres(genres);
        return m;
    }
}
