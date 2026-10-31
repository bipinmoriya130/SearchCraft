package com.searchcraft.controller;

import com.searchcraft.dto.IndexUrlRequest;
import com.searchcraft.dto.IndexedPageDto;
import com.searchcraft.service.IndexingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for submitting URLs for web indexing.
 */
@RestController
@RequestMapping("/api/index")
public class IndexingController {

    private final IndexingService indexingService;

    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * Submits a public web page URL for crawling and indexing.
     */
    @PostMapping
    public ResponseEntity<IndexedPageDto> submitUrlForIndexing(@Valid @RequestBody IndexUrlRequest request) {
        IndexedPageDto result = indexingService.indexUrl(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
