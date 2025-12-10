package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;
import pt.uc.movierecommendation.movierecommendationsystem.Service.MovieService;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/movies")
public class MovieController {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    private MovieService movieService;

    @Autowired
    private AuthService authService;


    // ================== SEARCH ==================

    @GetMapping("/search")
    public String searchMovies(@RequestParam String query) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/search/movie?query=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Retornar JSON direto pro frontend
        return response.body();
    }

    @GetMapping("/filtered_search")
    public String filteredSearchMovies(@RequestParam(required = false) Double ratingMin,
            @RequestParam(required = false) Double ratingMax,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String director) throws IOException, InterruptedException {

        String base = "https://api.themoviedb.org/3/discover/movie";
        List<String> params = new ArrayList<>();

        // Filter per rating
        if (ratingMin != null)
            params.add("vote_average.gte=" + ratingMin);
        if (ratingMax != null)
            params.add("vote_average.lte=" + ratingMax);

        // Filter per year
        if (year != null)
            params.add("primary_release_year=" + year);

        // Filter per genre
        if (genre != null && !genre.isBlank()) {
            Integer genreId = findGenreId(genre);
            if (genreId != null)
                params.add("with_genres=" + genreId);
        }

        // Filter per director
        if (director != null && !director.isBlank()) {
            Integer directorId = findPersonId(director);
            if (directorId == null)
                return "{}"; // director not found
            params.add("with_crew=" + directorId);
        }

        if (params.isEmpty()) {
            // if no filters provided, return popular movies
            return popularMovies();
        }

        params.add("api_key=" + apiKey);

        HttpClient client = HttpClient.newHttpClient();
        String url = base + "?" + String.join("&", params);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // ================== RECOMMENDATION ==================

    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedMovies(
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) throws IOException, InterruptedException {
        // Validating Authorization header
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String token = authorization.substring("Bearer ".length());
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

        try {
            // Getting recommended genre IDs for the user
            List<Integer> genreIds = movieService.getRecommendedGenresIds(userId);

            // If no genres found, return popular movies
            if (genreIds.isEmpty()) {
                String pop = popularMovies();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(pop);
            }

            // Getting IDs of movies the user has already interacted with to exclude them
            Set<Long> excludedIds = movieService.getExcludedMovieIds(userId);

            ObjectMapper mapper = new ObjectMapper();
            ArrayNode accumulatedResults = mapper.createArrayNode();

            int currentTmdbPage = 1;
            int maxPagesToCheck = 30; // Safety limit to prevent infinite loops
            int pagesChecked = 0;
            int targetCount = 20;


            // Calling TMDb API to get the recommendations
            String base = "https://api.themoviedb.org/3/discover/movie";
            String withGenres = genreIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            HttpClient client = HttpClient.newHttpClient();

            // Looping until have enough movies or checked too many pages
            while (accumulatedResults.size() < targetCount && pagesChecked < maxPagesToCheck) {

                String url = base +
                    "?with_genres=" + withGenres +
                    "&sort_by=popularity.desc" +
                    "&page=" + currentTmdbPage +
                    "&api_key=" + apiKey;

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


                if (response.statusCode() == 200) {
                    JsonNode root = mapper.readTree(response.body());
                    JsonNode results = root.path("results");

                    if (results.isArray()) {
                        for (JsonNode node : results) {
                            long movieId = node.path("id").asLong();
                            if (!excludedIds.contains(movieId)) {
                                accumulatedResults.add(node);
                                if (accumulatedResults.size() >= targetCount) break;
                            }
                        }
                    }
                }
                else {
                    return ResponseEntity
                            .status(HttpStatus.BAD_GATEWAY)
                            .body("Recommendation error");
                }

                currentTmdbPage++;
                pagesChecked++;
            }

            // Construct final JSON response
            ObjectNode finalResponse = mapper.createObjectNode();
            finalResponse.set("results", accumulatedResults);
            finalResponse.put("total_results", accumulatedResults.size());

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(finalResponse.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Recommendation error");
        }
    }

    // ================== POPULAR ==================

    @GetMapping("/popular")
    public String popularMovies() throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=1";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body(); // retorna JSON do TMDb diretamente
    }

    // ================== TOP ==================

    @GetMapping("/top")
    public String topMovies() throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey + "&page=1";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    // ================== MOVIE BY ID ==================

    @GetMapping("/{id}")
    public ResponseEntity<String> getMovieById(@PathVariable String id) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/movie/" +
                URLEncoder.encode(id, StandardCharsets.UTF_8) +
                "?api_key=" + apiKey + "&append_to_response=credits";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return ResponseEntity
                .status(response.statusCode())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(response.body());
    }

    // Helper methods to find IDs
    public Integer findPersonId(String name) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/search/person"
                + "?api_key=" + apiKey
                + "&query=" + URLEncoder.encode(name, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            return null;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode results = mapper.readTree(response.body()).path("results");
        if (!results.isArray() || results.size() == 0)
            return null;

        // Exact name match
        for (JsonNode person : results) {
            if (person.path("name").asText().equalsIgnoreCase(name)) {
                return person.path("id").asInt();
            }
        }
        // Directing department with partial match
        String query = name.toLowerCase();
        for (JsonNode person : results) {
            if ("Directing".equalsIgnoreCase(person.path("known_for_department").asText())
                    && person.path("name").asText().toLowerCase().contains(query)) {
                return person.path("id").asInt();
            }
        }
        // Fallback to first (most popular)
        return results.get(0).path("id").asInt();
    }

    private Integer findGenreId(String genreName) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Searching for the genre by name and getting its id
        JsonNode genres = mapper.readTree(response.body()).path("genres");
        if (genres.isArray()) {
            for (JsonNode genre : genres) {
                if (genre.path("name").asText().equalsIgnoreCase(genreName)) {
                    return genre.path("id").asInt();
                }
            }
        }
        return null;
    }

}
