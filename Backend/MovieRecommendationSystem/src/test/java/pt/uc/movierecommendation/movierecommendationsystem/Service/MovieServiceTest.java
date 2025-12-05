package pt.uc.movierecommendation.movierecommendationsystem.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.MovieRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private MovieRepository movieRepository;
    private MovieService movieService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepository.class);
        movieService = Mockito.spy(new MovieService(movieRepository));

        try {
            // Injecting a fake API key
            var apiKeyField = MovieService.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(movieService, "fake-api-key");
        } catch (Exception ignored) {}
    }

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
}
