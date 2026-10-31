package com.searchcraft.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for presenting search history items.
 */
public class SearchHistoryDto {

    private Long id;
    private String queryText;
    private Integer resultCount;
    private LocalDateTime searchedAt;

    public SearchHistoryDto() {
    }

    public SearchHistoryDto(Long id, String queryText, Integer resultCount, LocalDateTime searchedAt) {
        this.id = id;
        this.queryText = queryText;
        this.resultCount = resultCount;
        this.searchedAt = searchedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
}
