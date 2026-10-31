package com.searchcraft.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object wrapping a complete search response returned to the frontend.
 */
public class SearchResponse {

    private String query;
    private long totalResults;
    private long searchDurationMs;
    private int currentPage;
    private int totalPages;
    private List<SearchResultDto> results = new ArrayList<>();

    public SearchResponse() {
    }

    public SearchResponse(String query, long totalResults, long searchDurationMs,
                          int currentPage, int totalPages, List<SearchResultDto> results) {
        this.query = query;
        this.totalResults = totalResults;
        this.searchDurationMs = searchDurationMs;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.results = results != null ? results : new ArrayList<>();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public long getSearchDurationMs() {
        return searchDurationMs;
    }

    public void setSearchDurationMs(long searchDurationMs) {
        this.searchDurationMs = searchDurationMs;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<SearchResultDto> getResults() {
        return results;
    }

    public void setResults(List<SearchResultDto> results) {
        this.results = results;
    }
}
