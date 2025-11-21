package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/movies")
public class MovieController {

    @Value("${tmdb.api.key}")
    private String apiKey;



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

    @GetMapping("/{id}")
    public ResponseEntity<String> getMovieById(@PathVariable String id) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/movie/" +
                URLEncoder.encode(id, StandardCharsets.UTF_8) +
                "?api_key=" + apiKey;

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


}
