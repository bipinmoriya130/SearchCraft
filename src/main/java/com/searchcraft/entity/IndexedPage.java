package com.searchcraft.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a crawled and indexed web page in the database.
 *
 * Mapped to the 'indexed_pages' table in MySQL.
 */
@Entity
@Table(
    name = "indexed_pages",
    indexes = {
        @Index(name = "idx_canonical_url", columnList = "canonical_url", unique = true),
        @Index(name = "idx_indexing_status", columnList = "indexing_status"),
        @Index(name = "idx_indexed_at", columnList = "indexed_at")
    }
)
public class IndexedPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    /**
     * Canonical URL is normalized (lowercased host, stripped tracking params, stripped fragments)
     * and must be unique to avoid duplicate indexing of identical pages.
     */
    @Column(name = "canonical_url", nullable = false, unique = true, length = 768)
    private String canonicalUrl;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    /**
     * The cleaned textual content extracted from the HTML body, stripped of all tags and scripts.
     * Stored as LONGTEXT in MySQL to accommodate large documents.
     */
    @Lob
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "indexing_status", nullable = false, length = 20)
    private IndexingStatus indexingStatus;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "indexed_at", nullable = false)
    private LocalDateTime indexedAt;

    // Default constructor required by JPA/Hibernate
    public IndexedPage() {
    }

    public IndexedPage(String url, String canonicalUrl, String title, String description,
                       String extractedText, Integer httpStatusCode, IndexingStatus indexingStatus) {
        this.url = url;
        this.canonicalUrl = canonicalUrl;
        this.title = title;
        this.description = description;
        this.extractedText = extractedText;
        this.httpStatusCode = httpStatusCode;
        this.indexingStatus = indexingStatus;
        this.indexedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
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

    @PrePersist
    protected void onCreate() {
        if (this.indexedAt == null) {
            this.indexedAt = LocalDateTime.now();
        }
    }
}
