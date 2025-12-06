package pt.uc.movierecommendation.movierecommendationsystem.Service;

import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : null;
    }

    public List<HistoryItem> getCurrentUserHistory() {
        String email = currentEmail();
        if (email == null) return Collections.emptyList();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return Collections.emptyList();

        Long userId = optionalUser.get().getId();
        return historyItemRepository.findByUser_IdOrderByWatchedDateDesc(userId);
    }
}