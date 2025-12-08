package pt.uc.movierecommendation.movierecommendationsystem.Service;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.RatingsRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;



@Service
public class RatingService {

    
    private final RatingsRepository ratingsRepository;
    private final UserRepository userRepository;

    public RatingService(RatingsRepository ratingsRepository, UserRepository userRepository) {
        this.ratingsRepository = ratingsRepository;
        this.userRepository = userRepository;
    }

    public Float check(long movieId, long userId) {
        return ratingsRepository.findByUser_IdAndMovie_Id(userId, movieId)
                .map(Ratings::getRating)
                .orElse(null);
    }

    @Transactional
    public Ratings add(Movie movie, long userId, Float rating) {
        Ratings item = new Ratings();
        item.setMovie(movie);
        User user = userRepository.findById(userId)
         .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        item.setUser(user);
        item.setRating(rating);
        return ratingsRepository.save(item);
    }
    
    @Transactional
    public boolean update(long movieId, long userId, Float rating) {
        return ratingsRepository.findByUser_IdAndMovie_Id(userId, movieId)
        .map(oldRating -> {
            oldRating.setRating(rating);
            return true;
        })
        .orElse( false);
    }
}