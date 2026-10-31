package com.searchcraft.index;

/**
 * Represents an entry in a term's posting list for a specific document.
 *
 * Tracks term frequency partitioned by document fields (Title, Description, Body)
 * to allow field-weighted relevance scoring.
 */
public class Posting {

    private final Long documentId;
    private int titleFrequency;
    private int descriptionFrequency;
    private int bodyFrequency;

    public Posting(Long documentId) {
        this.documentId = documentId;
        this.titleFrequency = 0;
        this.descriptionFrequency = 0;
        this.bodyFrequency = 0;
    }

    public Posting(Long documentId, int titleFrequency, int descriptionFrequency, int bodyFrequency) {
        this.documentId = documentId;
        this.titleFrequency = titleFrequency;
        this.descriptionFrequency = descriptionFrequency;
        this.bodyFrequency = bodyFrequency;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public int getTitleFrequency() {
        return titleFrequency;
    }

    public void addTitleFrequency(int count) {
        this.titleFrequency += count;
    }

    public int getDescriptionFrequency() {
        return descriptionFrequency;
    }

    public void addDescriptionFrequency(int count) {
        this.descriptionFrequency += count;
    }

    public int getBodyFrequency() {
        return bodyFrequency;
    }

    public void addBodyFrequency(int count) {
        this.bodyFrequency += count;
    }

    public int getTotalFrequency() {
        return titleFrequency + descriptionFrequency + bodyFrequency;
    }

    /**
     * Calculates the field-weighted frequency score for this term in this document:
     * - Title weight: 5.0x
     * - Description weight: 2.5x
     * - Body weight: 1.0x
     */
    public double getWeightedScore() {
        return (titleFrequency * 5.0) + (descriptionFrequency * 2.5) + (bodyFrequency * 1.0);
    }
}
