package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;

import java.util.List;
import java.util.Optional;

public interface HistoryItemRepository extends JpaRepository<HistoryItem, Long> {
    List<HistoryItem> findByUser_IdOrderByWatchedDateDesc(Long userId);
    List<HistoryItem> findTop20ByUser_IdOrderByWatchedDateDesc(Long userId);
    Optional<HistoryItem> findByUser_IdAndMovie_Id(Long userId, Long movieId);

}
