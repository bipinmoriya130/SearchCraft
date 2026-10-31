package com.searchcraft.dto;

import com.searchcraft.entity.IndexingStatus;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an indexed document.
 */
public class IndexedPageDto {

    private Long id;
    private String url;
    private String canonicalUrl;
    private String title;
    private String description;
    private Integer httpStatusCode;
    private IndexingStatus indexingStatus;
    private String lastError;
    private LocalDateTime indexedAt;

    public IndexedPageDto() {
    }

    public IndexedPageDto(Long id, String url, String canonicalUrl, String title, String description,
                          Integer httpStatusCode, IndexingStatus indexingStatus, String lastError, LocalDateTime indexedAt) {
        this.id = id;
        this.url = url;
        this.canonicalUrl = canonicalUrl;
        this.title = title;
        this.description = description;
        this.httpStatusCode = httpStatusCode;
        this.indexingStatus = indexingStatus;
        this.lastError = lastError;
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

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public IndexingStatus getIndexingStatus() {
        return indexingStatus;
    }

    public void setIndexingStatus(IndexingStatus indexingStatus) {
        this.indexingStatus = indexingStatus;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(LocalDateTime indexedAt) {
        this.indexedAt = indexedAt;
    }
}
