package com.searchcraft.entity;

/**
 * Status of an indexing operation for a web page.
 */
public enum IndexingStatus {
    /**
     * Successfully fetched, parsed, and stored in the database and inverted index.
     */
    INDEXED,

    /**
     * Attempted to fetch or parse, but encountered an error (e.g. 404, timeout, SSRF block).
     */
    FAILED,

    /**
     * Submitted for indexing but waiting for processing.
     */
    PENDING
}
