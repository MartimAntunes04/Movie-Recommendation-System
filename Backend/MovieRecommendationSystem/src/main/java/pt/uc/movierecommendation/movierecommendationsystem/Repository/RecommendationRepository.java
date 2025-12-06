package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Recommendation;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUser_IdOrderByScoreDesc(Long userId);
}
