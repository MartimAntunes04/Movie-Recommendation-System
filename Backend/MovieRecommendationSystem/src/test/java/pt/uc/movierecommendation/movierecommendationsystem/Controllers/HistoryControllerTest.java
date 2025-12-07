package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.HistoryService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.WatchlistService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class HistoryControllerTest {

    private MockMvc mockMvc;
    private HistoryService historyService;
    private AuthService authService;
    private MovieService movieService;

    @BeforeEach
    void setup() {
        historyService = Mockito.mock(HistoryService.class);
        authService = Mockito.mock(AuthService.class);
        movieService = Mockito.mock(MovieService.class);
        HistoryController controller = new HistoryController(historyService, authService, movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ==================== GET ====================

    @Test
    void getHistory_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
        verifyNoInteractions(authService, historyService);
    }

    @Test
    void getHistory_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(get("/history")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(historyService);
    }

    @Test
    void getHistory_ok() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);

        HistoryItem historyItem = new HistoryItem();
        historyItem.setId(1L);

        Movie movie = new Movie();
        movie.setId(100L);
        movie.setTitle("The Matrix");

        historyItem.setMovie(movie);

        List<HistoryItem> history = List.of(historyItem);

        when(historyService.getCurrentUserHistory(42L)).thenReturn(history);


        mockMvc.perform(get("/history")
                        .header("Authorization", "Bearer tok")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.historyList[0].id").value(1))
                .andExpect(jsonPath("$.historyList[0].movie.id").value(100))
                .andExpect(jsonPath("$.historyList[0].movie.title").value("The Matrix"))
                .andExpect(jsonPath("$.pages").value("0/1"));

        verify(authService).getUserIdFromToken("tok");
        verify(historyService).getCurrentUserHistory(42L);
    }

    // ==================== POST /history/{movieId} ====================


    @Test
    void addMovie_unauthorized_whenMissingHeader() throws Exception{
        when(authService.getUserIdFromToken("tok")).thenReturn(null);

        mockMvc.perform(post("/history/550"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));

        verifyNoInteractions(authService, historyService);
    }


    @Test
    void addMovie_unauthorized_whenInvalidToken() throws Exception{
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(post("/history/550")
                .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(historyService);
    }

    @Test
    void addMovie_created_onSuccess() throws Exception{

        when(authService.getUserIdFromToken("tok")).thenReturn(42L);

        Movie movie = new Movie();
        movie.setId(550L);
        movie.setTitle("The Matrix");

        when(movieService.fetchOrCreateMovieById(550L)).thenReturn(movie);

        // Criar User fake
        User user = new User();
        user.setId(42L);
        user.setUsername("testuser");

        // Criar HistoryItem fake
        HistoryItem item = new HistoryItem();
        item.setMovie(movie);
        item.setUser(user);

        when(historyService.add(movie,42L)).thenReturn(item);

        mockMvc.perform(post("/history/550")
                    .header("Authorization", "Bearer tok"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Added to history"));

        verify(authService).getUserIdFromToken("tok");
        verify(movieService).fetchOrCreateMovieById(550L);
        verify(historyService).add(movie, 42L);

    }



    @Test
    void addMovie_conflict_onAlreadyExists() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);

        Movie movie = new Movie();
        movie.setId(550L);
        movie.setTitle("The Matrix");
        when(movieService.fetchOrCreateMovieById(550L)).thenReturn(movie);

        // Faz o mock do service para qualquer Movie com o mesmo userId
        doThrow(new IllegalStateException("Movie already in history"))
                .when(historyService).add(movie, 42L);

        mockMvc.perform(post("/history/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Movie already in history"));

        verify(historyService).add(movie, 42L);
    }

    // ==================== DELETE /history/{movieId} ====================

    @Test
    void removeMovie_unauthorized_whenMissingHeader() throws Exception{
        when(authService.getUserIdFromToken("tok")).thenReturn(null);

        mockMvc.perform(delete("/history/550"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));

        verifyNoInteractions(authService, historyService);
    }

    @Test
    void removeMovie_unauthorized_whenInvalidToken() throws Exception{
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(delete("/history/550")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(historyService);
    }

    @Test
    void removeMovie_noContent_onSuccess() throws Exception{
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);
        when(historyService.remove(550L, 42L)).thenReturn(true);

        mockMvc.perform(delete("/history/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Removed from history"));

        verify(authService).getUserIdFromToken("tok");
        verify(historyService).remove(550L, 42L);

    }


    @Test
    void removeMovie_conflict_onIllegalState() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);
        doThrow(new IllegalArgumentException("Movie not found in history")).when(historyService).remove(550L, 42L);

        mockMvc.perform(delete("/history/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Movie not found in history"));

        verify(historyService).remove(550L, 42L);
    }



}
