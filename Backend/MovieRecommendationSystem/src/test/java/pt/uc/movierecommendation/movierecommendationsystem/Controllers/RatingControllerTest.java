package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.HistoryService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.RatingService;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RatingControllerTest {

    private MockMvc mockMvc;
    private RatingService ratingService;
    private AuthService authService;
    private MovieService movieService;

    @BeforeEach
    void setup() {
        ratingService = Mockito.mock(RatingService.class);
        authService = Mockito.mock(AuthService.class);
        movieService = Mockito.mock(MovieService.class);
        RatingController controller = new RatingController(ratingService, authService, movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ==================== PUT ====================


    @Test
    void updateRating_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(put("/rating/550?rating=5"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
        verifyNoInteractions(authService, ratingService);
    }

    @Test
    void updateRating_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(put("/rating/550?rating=5")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(ratingService);
    }

    @Test
    void updateRating_onSucess() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);



        when(ratingService.update(eq(550L), eq(42L), eq(5.0f)))
                .thenReturn(true);

        mockMvc.perform(put("/rating/550")
                        .header("Authorization", "Bearer tok")
                        .param("rating", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rating updated"));

        verify(authService).getUserIdFromToken("tok");
        verify(ratingService).update(550L, 42L, 5.0f);
    }

    @Test
    void updateRating_addOnNotExists() throws Exception {

        when(authService.getUserIdFromToken("tok")).thenReturn(42L);
        when(ratingService.update(eq(550L), eq(42L), eq(5.0f))).thenReturn(false);

        Movie movie = new Movie();
        movie.setId(550L);
        when(movieService.fetchOrCreateMovieById(550L)).thenReturn(movie);

        mockMvc.perform(put("/rating/550")
                        .header("Authorization", "Bearer tok")
                        .param("rating", "5.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rating added"));

        verify(ratingService).update(550L, 42L, 5.0f);
        verify(movieService).fetchOrCreateMovieById(550L);
        verify(ratingService).add(movie, 42L, 5.0f);
    }


    // ==================== GET ====================

    @Test
    void getRating_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/rating/550"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
        verifyNoInteractions(authService, ratingService);
    }

    @Test
    void getRating_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(get("/rating/550")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(ratingService);
    }

    @Test
    void getRating_onSucess() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);

        when(ratingService.check(550L, 42L))
                .thenReturn(5.0f);


        mockMvc.perform(get("/rating/550")
                .header("Authorization", "Bearer tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.rating").value(5.0));

        verify(authService).getUserIdFromToken("tok");
        verify(ratingService).check(550L,42L);
    }

    @Test
    void getRating_notFound() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);


        when(ratingService.check(550L, 42L))
                .thenReturn(null);

        mockMvc.perform(get("/rating/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No rating for this movie"));

        verify(authService).getUserIdFromToken("tok");
        verify(ratingService).check(550L, 42L);
    }


}
