package pt.uc.movierecommendation.movierecommendationsystem.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;

import java.util.List;

public interface WatchListItemRepository extends JpaRepository<WatchListItem, Long> {
    List<WatchListItem> findByUser_Id(Long userId);
    Page<WatchListItem> findByUser_Id(Long userId, Pageable pageable);
    boolean existsByUser_IdAndMovie_Id(Long userId, Long movieId);
    void deleteByUser_IdAndMovie_Id(Long userId, Long movieId);
}
