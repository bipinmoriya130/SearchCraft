package com.searchcraft.service;

/**
 * Record holding the extracted content from an HTTP crawl response.
 */
public record FetchedPageData(
        String url,
        String canonicalUrl,
        String title,
        String description,
        String extractedText,
        int httpStatusCode
) {
}
