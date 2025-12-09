package pt.uc.movierecommendation.movierecommendationsystem.Controllers;


import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private MovieService movieService;

    @Spy
    private MovieController movieController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        // Initialize MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        
        try {
            java.lang.reflect.Field apiKeyField = MovieController.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(movieController, "fake-api-key");

            // Injecting authService mock
            java.lang.reflect.Field authServiceField = MovieController.class.getDeclaredField("authService");
            authServiceField.setAccessible(true);
            authServiceField.set(movieController, authService);

            // Injecting movieService mock
            java.lang.reflect.Field movieServiceField = MovieController.class.getDeclaredField("movieService");
            movieServiceField.setAccessible(true);
            movieServiceField.set(movieController, movieService);
        } catch (Exception e) {
            fail("Erro ao definir apiKey via reflexão: " + e.getMessage());
        }
    }

    // ==================== TESTE /search ====================

    @Test
    void testSearchCorreto()throws IOException, InterruptedException{

        String query = "Matrix";

        String result = movieController.searchMovies(query);

        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");

    }

    @Test
    void testPopularMovies()throws IOException, InterruptedException{
        String result = movieController.popularMovies();

        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
    }


    // ==================== TESTES /filtered_search ====================

    @Test
    void testFilteredSearch_NoFilters_PassToPopular() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with all null parameters
        String result = movieController.filteredSearchMovies(null, null, null, null, null);

        // Verifying the result and number of invocations
        assertEquals("{\"popular\":true}", result, "Deve passar para popularMovies quando não há filtros");
        verify(movieController, times(1)).popularMovies();
    }

    @Test
    void testFilteredSearch_BlankStrings_PassToPopular() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with blank strings for genre and director
        String result = movieController.filteredSearchMovies(null, null, null, "   ", "");

        // Verifying the result and number of invocations
        assertEquals("{\"popular\":true}", result, "Strings em branco devem ser ignoradas e passar para popularMovies");
        verify(movieController, times(1)).popularMovies();
    }

    @Test
    void testFilteredSearch_WithRatingOnly() throws IOException, InterruptedException {
        // Calling filteredSearchMovies with only ratingMin provided
        String result = movieController.filteredSearchMovies(7.0, null, null, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        verify(movieController, never()).popularMovies();
    }

    @Test
    void testFilteredSearch_WithYearOnly() throws IOException, InterruptedException {
        // Calling filteredSearchMovies with only year provided
        String result = movieController.filteredSearchMovies(null, null, 1999, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        verify(movieController, never()).popularMovies();
    }

    @Test
    void testFilteredSearch_WithRatingRangeAndYear() throws IOException, InterruptedException {
        // Calling filteredSearchMovies with rating range and year provided
        String result = movieController.filteredSearchMovies(6.5, 9.2, 2003, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        verify(movieController, never()).popularMovies();
    }

    @Test
    void testFilteredSearch_WithDirectorOnly() throws IOException, InterruptedException {
        // Because apikey is fake, we need to mock findPersonId to return a valid director ID
        Mockito.doReturn(24)
                .when(movieController).findPersonId("Robert Zemeckis");

        // Calling filteredSearchMovies with only director provided
        String result = movieController.filteredSearchMovies(null, null, null, null, "Robert Zemeckis");

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(!result.equals("{}"), "O retorno não deve ser um JSON vazio vazio");
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        verify(movieController, times(1)).findPersonId("Robert Zemeckis");
        verify(movieController, never()).popularMovies();
    }

    @Test
    void testFilteredSearch_WithMultipleFilters() throws IOException, InterruptedException {
        // Because apikey is fake, we need to mock findPersonId to return a valid director ID
        Mockito.doReturn(24)
                .when(movieController).findPersonId("Robert Zemeckis");
                
        // Calling filteredSearchMovies with multiple filters provided
        String result = movieController.filteredSearchMovies(7.5, 9.0, 2010, "Action", "Robert Zemeckis");

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(!result.equals("{}"), "O retorno não deve ser um JSON vazio vazio");
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        verify(movieController, times(1)).findPersonId("Robert Zemeckis");
        verify(movieController, never()).popularMovies();
    }

    // ==================== TESTS /recommended ====================
    @Test
    void getRecommendedMovies_MissingAuthHeader_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/movies/recommended"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
    }

    @Test
    void getRecommendedMovies_InvalidToken_ReturnsUnauthorized() throws Exception {
        when(authService.getUserIdFromToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/movies/recommended")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void getRecommendedMovies_NoGenresFound_ReturnsPopularMovies() throws Exception {
        Long userId = 1L;
        when(authService.getUserIdFromToken("valid_token")).thenReturn(userId);
        when(movieService.getRecommendedGenresIds(userId)).thenReturn(new ArrayList<>());
        
        Mockito.doReturn("{\"results\": []}").when(movieController).popularMovies();

        mockMvc.perform(get("/movies/recommended")
                .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"results\": []}"));
    }

    @Test
    void getRecommendedMovies_WithGenres_ReturnsRecommendations() throws Exception {
        Long userId = 1L;
        when(authService.getUserIdFromToken("valid_token")).thenReturn(userId);
        when(movieService.getRecommendedGenresIds(userId)).thenReturn(List.of(28, 12));
        
        mockMvc.perform(get("/movies/recommended")
                .header("Authorization", "Bearer valid_token")
                .param("page", "1"))
                .andExpect(status().isBadGateway()); 
    }
}
