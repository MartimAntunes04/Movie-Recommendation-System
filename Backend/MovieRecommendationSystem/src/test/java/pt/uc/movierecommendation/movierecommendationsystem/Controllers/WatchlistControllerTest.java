package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.WatchlistService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WatchlistControllerTest {

    private MockMvc mockMvc;
    private WatchlistService watchlistService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        watchlistService = Mockito.mock(WatchlistService.class);
        authService = Mockito.mock(AuthService.class);
        WatchlistController controller = new WatchlistController(watchlistService, authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ==================== GET ====================

    @Test
    void getWatchlist_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/watchlist"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
        verifyNoInteractions(authService, watchlistService);
    }

    @Test
    void getWatchlist_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(get("/watchlist")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(watchlistService);
    }

    @Test
    void getWatchlist_ok_returnsPageJson() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);

        Page<Map<String, Object>> page = new PageImpl<>(
                List.of(
                    Map.of(
                        "itemId", 1L,
                        "movieId", 100L,
                        "title", "The Matrix"
                    )
                ),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );
        when(watchlistService.get(42L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/watchlist")
                        .header("Authorization", "Bearer tok")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].itemId").value(1))
                .andExpect(jsonPath("$.content[0].movieId").value(100))
                .andExpect(jsonPath("$.content[0].title").value("The Matrix"));

        verify(authService).getUserIdFromToken("tok");
        verify(watchlistService).get(42L, 0, 20);
    }

    @Test
    void getWatchlist_notFound_whenServiceThrowsIllegalArgument() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(42L);
        when(watchlistService.get(42L, 0, 20)).thenThrow(new IllegalArgumentException("User not found: 42"));

        mockMvc.perform(get("/watchlist")
                        .header("Authorization", "Bearer tok")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found: 42"));

        verify(authService).getUserIdFromToken("tok");
        verify(watchlistService).get(42L, 0, 20);
    }

    // ==================== POST /watchlist/{movieId} ====================

    @Test
    void addMovie_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(post("/watchlist/550"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));

        verifyNoInteractions(authService, watchlistService);
    }

    @Test
    void addMovie_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(post("/watchlist/550")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(watchlistService);
    }

    @Test
    void addMovie_created_onSuccess() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        when(watchlistService.add(7L, 550L)).thenReturn(new WatchListItem());

        mockMvc.perform(post("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isCreated());

        verify(authService).getUserIdFromToken("tok");
        verify(watchlistService).add(7L, 550L);
    }

    @Test
    void addMovie_notFound_onIllegalArgument() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        doThrow(new IllegalArgumentException("Movie not found")).when(watchlistService).add(7L, 550L);

        mockMvc.perform(post("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Movie not found"));

        verify(watchlistService).add(7L, 550L);
    }

    @Test
    void addMovie_conflict_onAlreadyExists() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        doThrow(new IllegalStateException("Movie already in watchlist")).when(watchlistService).add(7L, 550L);

        mockMvc.perform(post("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Movie already in watchlist"));

        verify(watchlistService).add(7L, 550L);
    }

    // ==================== DELETE /watchlist/{movieId} ====================

    @Test
    void removeMovie_unauthorized_whenMissingHeader() throws Exception {
        mockMvc.perform(delete("/watchlist/550"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
                
        verifyNoInteractions(authService, watchlistService);
    }

    @Test
    void removeMovie_unauthorized_whenInvalidToken() throws Exception {
        when(authService.getUserIdFromToken("bad")).thenReturn(null);

        mockMvc.perform(delete("/watchlist/550")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(authService).getUserIdFromToken("bad");
        verifyNoInteractions(watchlistService);
    }

    @Test
    void removeMovie_noContent_onSuccess() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        doNothing().when(watchlistService).remove(7L, 550L);

        mockMvc.perform(delete("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isNoContent());

        verify(authService).getUserIdFromToken("tok");
        verify(watchlistService).remove(7L, 550L);
    }

    @Test
    void removeMovie_notFound_onIllegalArgument() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        doThrow(new IllegalArgumentException("Movie not found")).when(watchlistService).remove(7L, 550L);

        mockMvc.perform(delete("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Movie not found"));

        verify(watchlistService).remove(7L, 550L);
    }

    @Test
    void removeMovie_conflict_onIllegalState() throws Exception {
        when(authService.getUserIdFromToken("tok")).thenReturn(7L);
        doThrow(new IllegalStateException("Movie not in watchlist")).when(watchlistService).remove(7L, 550L);

        mockMvc.perform(delete("/watchlist/550")
                        .header("Authorization", "Bearer tok"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Movie not in watchlist"));

        verify(watchlistService).remove(7L, 550L);
    }
}
