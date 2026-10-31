package com.searchcraft.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Utility that extracts relevant textual snippets centered around query terms.
 */
@Component
public class SnippetGenerator {

    private static final int DEFAULT_SNIPPET_LENGTH = 180;

    /**
     * Extracts a window of text from bodyText that surrounds the first occurrence of any query token.
     */
    public String generateSnippet(String text, List<String> queryTokens) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (queryTokens == null || queryTokens.isEmpty()) {
            return cleaned.length() <= DEFAULT_SNIPPET_LENGTH
                    ? cleaned
                    : cleaned.substring(0, DEFAULT_SNIPPET_LENGTH) + "...";
        }

        String lowerText = cleaned.toLowerCase(Locale.ENGLISH);

        // Find the earliest match of any query token
        int bestIndex = -1;
        int matchedTokenLength = 0;

        for (String token : queryTokens) {
            int idx = lowerText.indexOf(token.toLowerCase(Locale.ENGLISH));
            if (idx != -1 && (bestIndex == -1 || idx < bestIndex)) {
                bestIndex = idx;
                matchedTokenLength = token.length();
            }
        }

        // If no token was found in body text, return beginning of text
        if (bestIndex == -1) {
            return cleaned.length() <= DEFAULT_SNIPPET_LENGTH
                    ? cleaned
                    : cleaned.substring(0, DEFAULT_SNIPPET_LENGTH) + "...";
        }

        // Calculate snippet window
        int lead = 50;
        int start = Math.max(0, bestIndex - lead);
        int end = Math.min(cleaned.length(), start + DEFAULT_SNIPPET_LENGTH);

        // Adjust to word boundary at start
        if (start > 0) {
            int spaceIdx = cleaned.indexOf(' ', start);
            if (spaceIdx != -1 && spaceIdx < bestIndex) {
                start = spaceIdx + 1;
            }
        }

        // Adjust to word boundary at end
        if (end < cleaned.length()) {
            int spaceIdx = cleaned.lastIndexOf(' ', end);
            if (spaceIdx != -1 && spaceIdx > bestIndex + matchedTokenLength) {
                end = spaceIdx;
            }
        }

        String snippet = cleaned.substring(start, end).trim();
        StringBuilder sb = new StringBuilder();
        if (start > 0) {
            sb.append("... ");
        }
        sb.append(snippet);
        if (end < cleaned.length()) {
            sb.append(" ...");
        }

        return sb.toString();
    }
}
