package pt.uc.movierecommendation.movierecommendationsystem.Service;

import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



@Service
public class HistoryService {

    
    private final HistoryItemRepository historyItemRepository;
    private final UserRepository userRepository;

    public HistoryService(HistoryItemRepository historyItemRepository, UserRepository userRepository) {
        this.historyItemRepository = historyItemRepository;
        this.userRepository = userRepository;
    }

    public List<HistoryItem> getCurrentUserHistory(long userId) {   
        return historyItemRepository.findByUser_IdOrderByWatchedDateDesc(userId);
    }

    public boolean check(long movieId, long userId) {
       Optional<HistoryItem> item = historyItemRepository.findByUser_IdAndMovie_Id(userId, movieId);
        if (item == null) return false;
        return true;
    }

    @Transactional
    public HistoryItem add(Movie movie, long userId) {
        HistoryItem item = new HistoryItem();
        item.setMovie(movie);
        User user = userRepository.findById(userId)
         .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        item.setUser(user);
        return historyItemRepository.save(item);
    }
    
    @Transactional
    public boolean remove(long movieId, long userId) {
        Optional<HistoryItem> item = historyItemRepository.findByUser_IdAndMovie_Id(userId, movieId);
        if (item == null) return false;
        historyItemRepository.delete(item.get());
        return true;

    }
}