package pt.uc.movierecommendation.movierecommendationsystem.Controllers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

    @InjectMocks
    private MovieController movieController;

    @BeforeEach
    void setUp(){
        movieController = new MovieController();

        try {
            java.lang.reflect.Field apiKeyField = MovieController.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(movieController, "fake-api-key");
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

        // Verifying the result
        assertEquals("{\"popular\":true}", result, "Deve passar para popularMovies quando não há filtros");
    }

    @Test
    void testFilteredSearch_BlankStrings_PassToPopular() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with blank strings for genre and director
        String result = movieController.filteredSearchMovies(null, null, null, "   ", "");

        // Verifying the result
        assertEquals("{\"popular\":true}", result, "Strings em branco devem ser ignoradas e passar para popularMovies");
    }

    @Test
    void testFilteredSearch_WithRatingOnly() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with only ratingMin provided
        String result = movieController.filteredSearchMovies(7.0, null, null, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        assertTrue(!result.contains("{\"popular\":true}"), "Deve retornar resultados filtrados, não populares");
    }

    @Test
    void testFilteredSearch_WithYearOnly() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with only year provided
        String result = movieController.filteredSearchMovies(null, null, 1999, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        assertTrue(!result.contains("{\"popular\":true}"), "Deve retornar resultados filtrados, não populares");
    }

    @Test
    void testFilteredSearch_WithRatingRangeAndYear() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with rating range and year provided
        String result = movieController.filteredSearchMovies(6.5, 9.2, 2003, null, null);

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        assertTrue(!result.contains("{\"popular\":true}"), "Deve retornar resultados filtrados, não populares");
    }

    @Test
    void testFilteredSearch_WithDirectorOnly() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();

        // Calling filteredSearchMovies with only director provided
        String result = movieController.filteredSearchMovies(null, null, null, null, "Nolan");

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        assertTrue(!result.contains("{\"popular\":true}"), "Deve retornar resultados filtrados, não populares");
    }

    @Test
    void testFilteredSearch_WithMultipleFilters() throws IOException, InterruptedException {
        // Defining the return value of popularMovies when called
        Mockito.doReturn("{\"popular\":true}")
                .when(movieController).popularMovies();
                
        // Calling filteredSearchMovies with multiple filters provided
        String result = movieController.filteredSearchMovies(7.5, 9.0, 2010, "Action", "Nolan");

        // Verifying the result and ensuring popularMovies was not called
        assertNotNull(result);
        assertTrue(result.contains("{") || result.isEmpty(), "O retorno deve ser um JSON ou vazio");
        assertTrue(!result.contains("{\"popular\":true}"), "Deve retornar resultados filtrados, não populares");
    }
}
