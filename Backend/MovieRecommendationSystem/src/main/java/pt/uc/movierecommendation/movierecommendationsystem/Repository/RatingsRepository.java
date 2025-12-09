package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.uc.movierecommendation.movierecommendationsystem.Model.Ratings;

import java.util.List;
import java.util.Optional;

public interface RatingsRepository extends JpaRepository<Ratings, Long> {
    List<Ratings> findByUser_Id(Long userId);
    List<Ratings> findByMovie_Id(Long movieId);
    Optional<Ratings> findByUser_IdAndMovie_Id(Long userId, Long movieId);
    
    //best rated
    List<Ratings> findTop20ByUser_IdAndRatingGreaterThanEqualOrderByRating(Long userId, Integer rateingThreshold);
    //worst rated
    List<Ratings> findTop20ByUser_IdAndRatingLessThanEqualOrderByRatingAsc(Long userId, Integer rateingThreshold);
    //best rated desc
    List<Ratings> findTop20ByUser_IdAndRatingGreaterThanEqualOrderByRatingDesc(Long userId, Integer rateingThreshold);

    @Query(
        value = "SELECT * FROM ratings WHERE user_id = :userId ORDER BY RANDOM() LIMIT 10",
        nativeQuery = true
    )
    List<Ratings> findRandom5ByUserId(@Param("userId") Long userId);
}
