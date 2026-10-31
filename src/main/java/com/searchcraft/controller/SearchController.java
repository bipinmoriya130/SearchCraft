package com.searchcraft.controller;

import com.searchcraft.dto.SearchRequest;
import com.searchcraft.dto.SearchResponse;
import com.searchcraft.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for executing search queries.
 *
 * Endpoint:
 * - GET /api/search?q={query}&page={page}&size={size}
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Executes a keyword search across the inverted index.
     *
     * @param q    The user's search query string.
     * @param page Zero-based page number (defaults to 0).
     * @param size Number of results per page (defaults to 10, max 50).
     * @return SearchResponse with ranked results, duration, and pagination metadata.
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(name = "q", defaultValue = "") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        SearchRequest request = new SearchRequest(q, page, size);
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }
}
