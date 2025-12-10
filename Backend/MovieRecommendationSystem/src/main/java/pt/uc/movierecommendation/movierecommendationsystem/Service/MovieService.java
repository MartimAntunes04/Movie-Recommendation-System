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

import pt.uc.movierecommendation.movierecommendationsystem.Model.Genre;
import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;
import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.MovieRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.RatingsRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.WatchListItemRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final MovieRepository movieRepository;
    private final RatingsRepository ratingsRepository;
    private final WatchListItemRepository watchListItemRepository;
    private final HistoryItemRepository historyItemRepository;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public MovieService(MovieRepository movieRepository, RatingsRepository ratingsRepository, WatchListItemRepository watchListItemRepository, HistoryItemRepository historyItemRepository) {
        this.movieRepository = movieRepository;
        this.ratingsRepository = ratingsRepository;
        this.watchListItemRepository = watchListItemRepository;
        this.historyItemRepository = historyItemRepository;
    }

    public List<Integer> getRecommendedGenresIds(Long userId) throws InterruptedException {
        // Verifying input
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        // Getting top rated movies for the user
        List<Ratings> topRated = ratingsRepository.findTop20ByUser_IdAndRatingGreaterThanEqualOrderByRatingDesc(userId, 7);
        List<Genre> likedGenres = new ArrayList<>();
        for (Ratings item : topRated) {
            Movie movie = item.getMovie();
            if (movie != null && movie.getGenres() != null) {
                likedGenres.addAll(movie.getGenres());
            }
        }

        // Including genres from watchlist movies
        List<WatchListItem> watchList = watchListItemRepository.findByUser_Id(userId);
        for (WatchListItem item : watchList) {
            Movie movie = item.getMovie();
            if (movie != null && movie.getGenres() != null) {
                likedGenres.addAll(movie.getGenres());
            }
        }

        // If no genres found, return empty list
        if (likedGenres.isEmpty()) return new ArrayList<>();

        // Counting genres by name and pick top 3 (most frequents)
        Map<String, Long> genreCounts = likedGenres.stream()
                .filter(genre -> genre != null && genre.getName() != null && !genre.getName().isBlank())
                .map(genre -> genre.getName().trim())
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        List<String> selectedGenreNames = genreCounts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // Mapping DB genres directly to TMDb genre IDs
        List<Integer> genreIds = new ArrayList<>();
        for (String name : selectedGenreNames) {
            for (Genre genre : likedGenres) {
                if (genre != null && genre.getName() != null && genre.getName().trim().equalsIgnoreCase(name)
                        && genre.getId() != null) {
                    genreIds.add(genre.getId().intValue());
                    break;
                }
            }
        }

        return genreIds;
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
        movie.setTmdbId(movieId);

        if (details.hasNonNull("title") && !details.get("title").asText().isEmpty()) {
            movie.setTitle(details.get("title").asText());
        } else {
            movie.setTitle("No title found");
        }

        if (details.hasNonNull("overview") && !details.get("overview").asText().isEmpty()) {
            movie.setDescription(details.get("overview").asText());
        } else {
            movie.setDescription("No Description found");
        }

        if (details.hasNonNull("vote_average")) {
            movie.setAverageRating(details.get("vote_average").asDouble());        
        }else movie.setAverageRating(5.0);

        if (details.hasNonNull("vote_count")) {
            movie.setVoteCount(details.get("vote_count").asLong());        
        }else movie.setVoteCount(0L);
        
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

        // store poster path from TMDb
        movie.setPosterPath(details.path("poster_path").asText(null));

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

    public Set<Long> getExcludedMovieIds(Long userId) {
        Set<Long> excludedIds = new HashSet<>();

        // Adding movies from Watchlist
        List<WatchListItem> watchlist = watchListItemRepository.findByUser_Id(userId);
        for (WatchListItem item : watchlist) {
            if (item.getMovie() != null) {
                excludedIds.add(item.getMovie().getId());
            }
        }

        // Adding movies from historical
        List<HistoryItem> historyItems = historyItemRepository.findByUser_Id(userId);
        for (HistoryItem item : historyItems) {
            if (item.getMovie() != null) {
                excludedIds.add(item.getMovie().getId());
            }
        }

        return excludedIds;
    }
}
