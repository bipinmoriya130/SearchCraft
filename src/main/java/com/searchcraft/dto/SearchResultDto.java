package com.searchcraft.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an individual search result card.
 */
public class SearchResultDto {

    private Long id;
    private String url;
    private String title;
    private String snippet;
    private double relevanceScore;
    private LocalDateTime indexedAt;

    public SearchResultDto() {
    }

    public SearchResultDto(Long id, String url, String title, String snippet, double relevanceScore, LocalDateTime indexedAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.relevanceScore = relevanceScore;
        this.indexedAt = indexedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public LocalDateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(LocalDateTime indexedAt) {
        this.indexedAt = indexedAt;
    }
}
