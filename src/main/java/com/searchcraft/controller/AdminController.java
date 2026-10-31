package com.searchcraft.controller;

import com.searchcraft.dto.DashboardStatsDto;
import com.searchcraft.dto.IndexedPageDto;
import com.searchcraft.service.IndexingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin operations and dashboard statistics.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final IndexingService indexingService;

    public AdminController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * Retrieves paginated indexed documents.
     */
    @GetMapping("/pages")
    public ResponseEntity<Page<IndexedPageDto>> listIndexedPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(indexingService.getIndexedPages(pageable));
    }

    /**
     * Retrieves an individual indexed document by ID.
     */
    @GetMapping("/pages/{id}")
    public ResponseEntity<IndexedPageDto> getPageById(@PathVariable Long id) {
        return ResponseEntity.ok(indexingService.getPageById(id));
    }

    /**
     * Deletes an indexed document from MySQL.
     */
    @DeleteMapping("/pages/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        indexingService.deletePage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retries indexing a failed page.
     */
    @PostMapping("/pages/{id}/retry")
    public ResponseEntity<IndexedPageDto> retryIndexing(@PathVariable Long id) {
        return ResponseEntity.ok(indexingService.retryIndexing(id));
    }

    /**
     * Returns dashboard statistics (total indexed, failed, searches, terms).
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(indexingService.getDashboardStats());
    }
}
