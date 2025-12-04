package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;

import java.util.List;
import java.util.Optional;

public interface RatingsRepository extends JpaRepository<Ratings, Long> {
    List<Ratings> findByUser_Id(Long userId);
    List<Ratings> findByMovie_Id(Long movieId);
    Optional<Ratings> findByUser_IdAndMovie_Id(Long userId, Long movieId);
    List<Ratings> findTop20ByUser_IdAndRateGreaterThanEqualOrderByRatingDateDesc(Long userId, Integer rateThreshold);
}
