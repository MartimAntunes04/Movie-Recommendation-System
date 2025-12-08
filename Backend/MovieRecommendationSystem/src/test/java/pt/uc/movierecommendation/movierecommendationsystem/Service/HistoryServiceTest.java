package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uc.movierecommendation.movierecommendationsystem.Model.HistoryItem;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.HistoryItemRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.WatchListItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HistoryServiceTest {

    private HistoryItemRepository historyItemRepository;
    private UserRepository userRepository;

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyItemRepository = mock(HistoryItemRepository.class);
        userRepository = mock(UserRepository.class);
        historyService = new HistoryService(historyItemRepository, userRepository);
    }

    // ------------------- getCurrentUserHistory -------------------
    @Test
    void getCurrentUserHistory_returnsList() {
        HistoryItem item = new HistoryItem();
        item.setId(1L);
        when(historyItemRepository.findByUser_Id(42L))
                .thenReturn(List.of(item));

        List<HistoryItem> result = historyService.getCurrentUserHistory(42L);

        assertEquals(1, result.size());
        verify(historyItemRepository).findByUser_Id(42L);
    }

    // ------------------- check -------------------
    @Test
    void check_returnsTrue_whenItemExists() {
        HistoryItem item = new HistoryItem();
        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.of(item));

        boolean exists = historyService.check(100L, 42L);

        assertTrue(exists);
    }

    @Test
    void check_returnsFalse_whenItemNotExists() {
        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.empty());

        boolean exists = historyService.check(100L, 42L);

        assertFalse(exists);
    }

    // ------------------- add -------------------
    @Test
    void add_createsHistoryItem_success() {
        User user = new User();
        user.setId(42L);

        Movie movie = new Movie();
        movie.setId(100L);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L)).thenReturn(Optional.empty());
        HistoryItem savedItem = new HistoryItem();
        savedItem.setId(1L);
        when(historyItemRepository.save(any())).thenReturn(savedItem);

        HistoryItem result = historyService.add(movie, 42L);

        assertEquals(1L, result.getId());
        verify(historyItemRepository).save(any());
    }

    @Test
    void add_throwsException_whenUserNotFound() {
        Movie movie = new Movie();
        movie.setId(100L);

        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> historyService.add(movie, 42L));
    }

    @Test
    void add_throwsException_whenMovieAlreadyExists() {
        User user = new User();
        user.setId(42L);

        Movie movie = new Movie();
        movie.setId(100L);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.of(new HistoryItem()));

        assertThrows(IllegalStateException.class, () -> historyService.add(movie, 42L));
    }

    // ------------------- remove -------------------
    @Test
    void remove_returnsTrue_whenItemExists() {
        HistoryItem item = new HistoryItem();

        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.of(item));

        boolean result = historyService.remove(100L, 42L);

        assertTrue(result);
        verify(historyItemRepository).delete(item);
    }

    @Test
    void remove_returnsFalse_whenItemNotExists() {
        when(historyItemRepository.findByUser_IdAndMovie_Id(42L, 100L))
                .thenReturn(Optional.empty());

        boolean result = historyService.remove(100L, 42L);

        assertFalse(result);
        verify(historyItemRepository, never()).delete(any());
    }
}
