package pt.uc.movierecommendation.movierecommendationsystem.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.MovieRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final MovieRepository movieRepository;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie fetchOrCreateMovieById(Long movieId) throws IOException, InterruptedException {
        if (movieId == null || movieId <= 0) {
            throw new IllegalArgumentException("Invalid movieId");
        }
        
        //Search Database
        Optional<Movie> existing = movieRepository.findById(movieId);
        if (existing.isPresent()) return existing.get();

        //Otherwise fetch from API
        // Getting details
        JsonNode details = getMovieDetails(movieId);

        // Getting credits (for director and cast)
        JsonNode credits = getMovieCredits(movieId);

        Movie movie = new Movie();
        movie.setId(movieId);
        if (details.hasNonNull("title") && !details.get("title").asText().isEmpty()) {
            movie.setTitle(details.get("title").asText());
        } else movie.setTitle("No title found");
        
        if (details.hasNonNull("overview") && !details.get("overview").asText().isEmpty()) {
            movie.setDescription(details.get("overview").asText());
        } else movie.setDescription("No Description  found");
        
        if (details.hasNonNull("vote_average")) {
            movie.setAverageRating(details.get("vote_average").asDouble());        
        }else movie.setAverageRating(5.0);
        
        String date = details.path("release_date").asText(null);
        if (date != null && !date.isBlank()) {
            try {
                movie.setReleaseDate(LocalDate.parse(date));
            } catch (Exception ignore) {}
        }

        // Getting Director (first crew with job=Director)
        String director = StreamSupport.stream(credits.path("crew").spliterator(), false)
                .filter(name -> "Director".equalsIgnoreCase(name.path("job").asText()))
                .map(name -> name.path("name").asText())
                .findFirst().orElse(null);
        movie.setDirector(director);

        // Getting Top 5 cast names
        String cast = StreamSupport.stream(credits.path("cast").spliterator(), false)
                .limit(5)
                .map(name -> name.path("name").asText())
                .collect(Collectors.joining(", "));
        movie.setCastMembers(cast);

        return movieRepository.save(movie);
    }

    public JsonNode getMovieDetails(Long movieId) throws IOException, InterruptedException {
        String detailsUrl = "https://api.themoviedb.org/3/movie/"
                            + URLEncoder.encode(String.valueOf(movieId), StandardCharsets.UTF_8)
                            + "?api_key=" + apiKey;
        HttpRequest detailsReq = HttpRequest.newBuilder().uri(URI.create(detailsUrl)).GET().build();
        HttpResponse<String> detailsRes = client.send(detailsReq, HttpResponse.BodyHandlers.ofString());
        if (detailsRes.statusCode() != 200) throw new IllegalArgumentException("TMDb details failed: " + detailsRes.statusCode());
        return mapper.readTree(detailsRes.body());
    }

    public JsonNode getMovieCredits(Long movieId) throws IOException, InterruptedException {
        String creditsUrl = "https://api.themoviedb.org/3/movie/"
                            + URLEncoder.encode(String.valueOf(movieId), StandardCharsets.UTF_8)
                            + "/credits?api_key=" + apiKey;
        HttpRequest creditsReq = HttpRequest.newBuilder().uri(URI.create(creditsUrl)).GET().build();
        HttpResponse<String> creditsRes = client.send(creditsReq, HttpResponse.BodyHandlers.ofString());
        if (creditsRes.statusCode() != 200) throw new IllegalArgumentException("TMDb credits failed: " + creditsRes.statusCode());
        return mapper.readTree(creditsRes.body());
    }

}
