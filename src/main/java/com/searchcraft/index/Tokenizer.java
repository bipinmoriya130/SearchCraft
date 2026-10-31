package com.searchcraft.index;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * High-performance text tokenizer and normalizer.
 *
 * Responsibilities:
 * 1. Case Normalization: Converts all characters to lowercase.
 * 2. Punctuation Stripping: Removes non-alphanumeric characters.
 * 3. Stop Word Filtering: Removes frequent grammatical words ("the", "is", "at")
 *    that add noise without search value.
 * 4. Token Length Filtering: Discards single characters and excessively long garbage strings.
 */
@Component
public class Tokenizer {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    // Standard English stop words
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
            "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me",
            "more", "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
            "other", "our", "ours", "ourselves", "out", "over", "own", "s", "same", "she", "should", "so",
            "some", "such", "t", "than", "that", "the", "their", "theirs", "them", "themselves", "then",
            "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up",
            "very", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why",
            "will", "with", "would", "you", "your", "yours", "yourself", "yourselves"
    );

    /**
     * Splits raw text into an ordered list of normalized tokens.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Lowercase
        String lower = text.toLowerCase(Locale.ENGLISH);

        // 2. Strip non-alphanumeric punctuation
        String cleaned = NON_ALPHANUMERIC.matcher(lower).replaceAll(" ");

        // 3. Split by whitespace
        String[] rawTokens = WHITESPACE.split(cleaned.trim());

        List<String> tokens = new ArrayList<>(rawTokens.length);
        for (String raw : rawTokens) {
            String token = raw.trim();
            // Filter out empty, single-character, overlong, or stop words
            if (isValidToken(token)) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    /**
     * Computes the Term Frequency (TF) map: { term -> frequency_count } for a document.
     */
    public Map<String, Integer> computeTermFrequencies(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Integer> tfMap = new HashMap<>(tokens.size());

        for (String token : tokens) {
            tfMap.merge(token, 1, Integer::sum);
        }

        return tfMap;
    }

    private boolean isValidToken(String token) {
        if (token.length() < 2 || token.length() > 50) {
            return false;
        }
        return !STOP_WORDS.contains(token);
    }
}
