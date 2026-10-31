package com.searchcraft.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for search queries with pagination support.
 */
public class SearchRequest {

    @NotBlank(message = "Search query cannot be empty")
    @Size(max = 255, message = "Search query must not exceed 255 characters")
    private String query;

    @Min(value = 0, message = "Page index cannot be negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 50, message = "Page size cannot exceed 50 items")
    private int size = 10;

    public SearchRequest() {
    }

    public SearchRequest(String query, int page, int size) {
        this.query = query;
        this.page = page;
        this.size = size;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
