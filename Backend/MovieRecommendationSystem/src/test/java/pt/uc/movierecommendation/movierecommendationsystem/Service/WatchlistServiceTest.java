package pt.uc.movierecommendation.movierecommendationsystem.Service;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import pt.uc.movierecommendation.movierecommendationsystem.Model.Movie;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Model.WatchListItem;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.WatchListItemRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WatchlistServiceTest {

    private WatchListItemRepository watchListItemRepository;
    private UserRepository userRepository;
    private MovieService movieService;

    private WatchlistService watchlistService;

    @BeforeEach
    void setUp() {
        watchListItemRepository = mock(WatchListItemRepository.class);
        userRepository = mock(UserRepository.class);
        movieService = mock(MovieService.class);
        watchlistService = new WatchlistService(watchListItemRepository, userRepository, movieService);
    }


    // ========== TESTS GET ==========

    @Test
    void get_shouldThrow_whenInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> watchlistService.get(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.get(1L, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.get(1L, 0, 0));
    }

    @Test
    void get_shouldReturnPage_withMappedContent() {
        Long userId = 10L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Preparing one watchlist item
        Movie movie = new Movie();
        movie.setId(100L);
        movie.setTitle("The Matrix");

        WatchListItem item = new WatchListItem();
        item.setId(200L);
        item.setAddedDate(LocalDate.of(2025, 1, 1));
        item.setMovie(movie);

        Page<WatchListItem> page = new PageImpl<>(
            List.of(item),
            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "addedDate")),
            1
        );
        when(watchListItemRepository.findByUser_Id(eq(userId), any(Pageable.class))).thenReturn(page);

        Page<Map<String, Object>> result = watchlistService.get(userId, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        Map<String, Object> row = result.getContent().get(0);
        assertThat(row.get("itemId")).isEqualTo(200L);
        assertThat(row.get("movieId")).isEqualTo(100L);
        assertThat(row.get("title")).isEqualTo("The Matrix");
        assertThat(row.get("addedDate")).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void get_shouldClampToLastPage_whenRequestedPageTooHigh() {
        Long userId = 10L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        // First call returns totalPages=1, second call returns last page content
        Page<WatchListItem> emptyFirst = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(5, 5),
                5
        );
        when(watchListItemRepository.findByUser_Id(eq(userId), argThat(p -> p.getPageNumber() == 5)))
                .thenReturn(emptyFirst);

        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("Last Page Movie");
        WatchListItem item = new WatchListItem();
        item.setId(2L);
        item.setMovie(m);
        item.setAddedDate(LocalDate.now());

        Page<WatchListItem> lastPage = new PageImpl<>(
            List.of(item),
            PageRequest.of(0, 5),
            5
        );

        when(watchListItemRepository.findByUser_Id(eq(userId), argThat(p -> p.getPageNumber() == 0)))
                .thenReturn(lastPage);

        Page<Map<String, Object>> result = watchlistService.get(userId, 5, 5);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).get("title")).isEqualTo("Last Page Movie");
    }


    // ========== TESTS ADD ==========

    @Test
    void add_shouldThrow_whenInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> watchlistService.add(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.add(1L, null));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.add(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.add(1L, 0L));
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> watchlistService.add(1L, 10L));
    }

    @Test
    void add_shouldCreateItem_whenNotExists() throws Exception {
        Long userId = 1L;
        Long movieId = 10L;
        User user = new User();
        user.setId(userId);
        Movie movie = new Movie();
        movie.setId(100L);
        movie.setTitle("Interstellar");
        WatchListItem saved = new WatchListItem();
        saved.setId(200L);
        saved.setUser(user);
        saved.setMovie(movie); 
        saved.setAddedDate(LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieService.fetchOrCreateMovieById(movieId)).thenReturn(movie);
        when(watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movie.getId())).thenReturn(false);
        when(watchListItemRepository.save(any(WatchListItem.class))).thenReturn(saved);

        WatchListItem result = watchlistService.add(userId, movieId);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        verify(watchListItemRepository).save(any(WatchListItem.class));
    }

    @Test
    void add_shouldFail_whenAlreadyInWatchlist() throws Exception {
        Long userId = 1L;
        Long movieId = 100L;
        User user = new User();
        user.setId(userId);
        Movie movie = new Movie();
        movie.setId(movieId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieService.fetchOrCreateMovieById(movieId)).thenReturn(movie);
        when(watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movie.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> watchlistService.add(userId, movieId));
        verify(watchListItemRepository, never()).save(any());
    }

    @Test
    void add_shouldWrapTmdbErrors_asIllegalArgument() throws Exception {
        Long userId = 1L;
        Long movieId = 100L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieService.fetchOrCreateMovieById(movieId)).thenThrow(new IOException("TMDb 401"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> watchlistService.add(userId, movieId));
        assertTrue(ex.getMessage().contains("Failed to fetch movie data"));
    }


    // ========== TESTS REMOVE ==========

    @Test
    void remove_shouldThrow_whenInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> watchlistService.remove(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.remove(1L, null));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.remove(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> watchlistService.remove(1L, 0L));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> watchlistService.remove(1L, 100L));
    }

    @Test
    void remove_shouldThrow_whenMovieNotInWatchlist() {
        Long userId = 1L;
        Long movieId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movieId)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> watchlistService.remove(userId, movieId));
        verify(watchListItemRepository, never()).deleteByUser_IdAndMovie_Id(any(), any());
    }

    @Test
    void remove_shouldDelete_whenPresent() {
        Long userId = 1L;
        Long movieId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(watchListItemRepository.existsByUser_IdAndMovie_Id(userId, movieId)).thenReturn(true);

        watchlistService.remove(userId, movieId);

        verify(watchListItemRepository).deleteByUser_IdAndMovie_Id(userId, movieId);
    }
}
