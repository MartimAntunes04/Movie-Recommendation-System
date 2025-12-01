package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Genre;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Service.HistoryService;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.GenreRepository;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Value("${tmdb.api.key}")
    private String apiKey;


    private final HistoryService historyService;
    private final GenreRepository genreRepository;

    public HistoryController(HistoryService historyService, GenreRepository genreRepository) {
        this.historyService = historyService;
        this.genreRepository = genreRepository;
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewHistoryPage() {

        // Get list of history items
        var historyList = historyService.getCurrentUserHistory();

        if (historyList.isEmpty()) {
        // Return a message if no movies in history
            return ResponseEntity.ok(Map.of("message", "No movies in your history."));
        }


        // Return JSON list to frontend
        return ResponseEntity.ok(historyList);
    }

    @GetMapping("/add_remove/{id}")
    public ResponseEntity<?> add_removeHistory(@PathVariable long id) throws IOException, InterruptedException {

        String url = "https://api.themoviedb.org/3/movie/" + id +
                      "?api_key=" + apiKey + "&language=en-US";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = 
        client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode details = mapper.readTree(response.body());

        //movie
        Movie movie = new Movie();
        movie.setId(id);

        if (details.hasNonNull("title") && !details.get("title").asText().isEmpty()) {
            movie.setTitle(details.get("title").asText());
        } else movie.setTitle("No title found");
        
        if (details.hasNonNull("overview") && !details.get("overview").asText().isEmpty()) {
            movie.setDescription(details.get("overview").asText());
        } else movie.setDescription("No Description  found");
        
        if (details.hasNonNull("vote_average")) {
            movie.setAverageRating(details.get("vote_average").asDouble());        
        }else movie.setAverageRating(5.0);

        if (details.hasNonNull("release_date") && !details.get("release_date").asText().isEmpty()) {
            movie.setReleaseDate(LocalDate.parse(details.get("release_date").asText()));
        }

        //genres 
        Set<Genre> genreSet = new HashSet<>();

        if (details.has("genres")) {
            for (JsonNode g : details.get("genres")) {

                String genreName = g.get("name").asText();

                Genre genre = genreRepository.findByName(genreName)
                        .orElseGet(() -> {
                            Genre newGenre = new Genre();
                            newGenre.setName(genreName);
                            return genreRepository.save(newGenre);
                        });

                genreSet.add(genre);
            }
        }
        movie.setGenres(genreSet);
        historyService.addMovie(movie);

        return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Added to history"
            ));

    }
}
