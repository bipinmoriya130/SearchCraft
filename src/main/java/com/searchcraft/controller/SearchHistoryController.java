package com.searchcraft.controller;

import com.searchcraft.dto.SearchHistoryDto;
import com.searchcraft.service.SearchHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user search history.
 *
 * Endpoints:
 * - GET    /api/history : Retrieves recent search queries.
 * - DELETE /api/history : Clears all saved search history.
 */
@RestController
@RequestMapping("/api/history")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    /**
     * Retrieves the most recent search queries for suggestions and history display.
     */
    @GetMapping
    public ResponseEntity<List<SearchHistoryDto>> getRecentSearches() {
        List<SearchHistoryDto> history = searchHistoryService.getRecentSearches();
        return ResponseEntity.ok(history);
    }

    /**
     * Clears all search history records.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearHistory() {
        searchHistoryService.clearHistory();
        return ResponseEntity.noContent().build();
    }
}
