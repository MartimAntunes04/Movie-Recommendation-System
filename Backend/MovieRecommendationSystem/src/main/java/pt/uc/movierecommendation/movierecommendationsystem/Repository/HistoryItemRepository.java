package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;

import java.util.List;

public interface HistoryItemRepository extends JpaRepository<HistoryItem, Long> {
    List<HistoryItem> findByUser_IdOrderByWatchedDateDesc(Long userId);
}
