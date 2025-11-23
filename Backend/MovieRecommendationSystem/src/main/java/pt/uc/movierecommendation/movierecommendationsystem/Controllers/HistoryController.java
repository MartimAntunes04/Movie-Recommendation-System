package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import pt.uc.movierecommendation.movierecommendationsystem.Service.HistoryService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewHistoryPage() {

        // Get list of history items
        var historyList = historyService.getCurrentUserHistory();

        if (historyList.isEmpty()) {
        // Return a message if no movies in history
            return ResponseEntity.ok(Map.of("message", "No movies in your history."));
        }


        // Return JSON list to frontend
        return ResponseEntity.ok(historyList);
    }
}
