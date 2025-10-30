package pt.uc.movierecommendation.movierecommendationsystem.Controllers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
