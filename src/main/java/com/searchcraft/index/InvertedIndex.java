package com.searchcraft.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe In-Memory Inverted Index Engine.
 *
 * Architecture:
 * - Forward Index (docToTerms): Maps docId -> Set of distinct terms.
 *   Enables O(K) fast document removal instead of scanning the whole vocabulary.
 * - Inverted Index (index): Maps term -> (docId -> Posting).
 *   Enables O(1) posting list retrieval per query token.
 * - Document Lengths (docLengths): Tracks total words per document for length normalization.
 */
@Component
public class InvertedIndex {

    private static final Logger log = LoggerFactory.getLogger(InvertedIndex.class);

    private final Tokenizer tokenizer;

    // term -> { documentId -> Posting }
    private final Map<String, Map<Long, Posting>> index = new ConcurrentHashMap<>();

    // documentId -> Set of terms contained in this document (for O(K) deletion/update)
    private final Map<Long, Set<String>> docToTerms = new ConcurrentHashMap<>();

    // documentId -> total token count
    private final Map<Long, Integer> docLengths = new ConcurrentHashMap<>();

    private final AtomicLong totalIndexedTokens = new AtomicLong(0);

    public InvertedIndex(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * Adds or updates a document in the inverted index.
     *
     * @param docId       Unique document ID from MySQL.
     * @param title       Page title.
     * @param description Meta description.
     * @param bodyText    Cleaned body text.
     */
    public synchronized void addDocument(Long docId, String title, String description, String bodyText) {
        if (docId == null) {
            return;
        }

        // If document already indexed, remove old version first (update idempotence)
        if (docToTerms.containsKey(docId)) {
            removeDocument(docId);
        }

        Map<String, Integer> titleTf = tokenizer.computeTermFrequencies(title);
        Map<String, Integer> descTf = tokenizer.computeTermFrequencies(description);
        Map<String, Integer> bodyTf = tokenizer.computeTermFrequencies(bodyText);

        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(titleTf.keySet());
        allTerms.addAll(descTf.keySet());
        allTerms.addAll(bodyTf.keySet());

        int docTokenCount = 0;

        for (String term : allTerms) {
            int titleFreq = titleTf.getOrDefault(term, 0);
            int descFreq = descTf.getOrDefault(term, 0);
            int bodyFreq = bodyTf.getOrDefault(term, 0);

            docTokenCount += (titleFreq + descFreq + bodyFreq);

            Posting posting = new Posting(docId, titleFreq, descFreq, bodyFreq);

            // Put posting in the term's posting list
            index.computeIfAbsent(term, k -> new ConcurrentHashMap<>()).put(docId, posting);
        }

        docToTerms.put(docId, allTerms);
        docLengths.put(docId, docTokenCount);
        totalIndexedTokens.addAndGet(docTokenCount);

        log.debug("Indexed docId [{}] with {} unique terms and {} total tokens", docId, allTerms.size(), docTokenCount);
    }

    /**
     * Removes a document from the inverted index in O(K) time, where K is the number of terms in the document.
     * Prunes empty term buckets from memory.
     *
     * @param docId The ID of the document to remove.
     */
    public synchronized void removeDocument(Long docId) {
        if (docId == null) {
            return;
        }

        Set<String> terms = docToTerms.remove(docId);
        if (terms == null) {
            return;
        }

        Integer length = docLengths.remove(docId);
        if (length != null) {
            totalIndexedTokens.addAndGet(-length);
        }

        for (String term : terms) {
            Map<Long, Posting> postings = index.get(term);
            if (postings != null) {
                postings.remove(docId);
                // Prune dead term to conserve RAM
                if (postings.isEmpty()) {
                    index.remove(term);
                }
            }
        }

        log.debug("Removed docId [{}] from inverted index", docId);
    }

    /**
     * Retrieves the posting map { docId -> Posting } for a normalized term.
     * Returns an unmodifiable map or empty map if the term is absent.
     */
    public Map<Long, Posting> getPostings(String term) {
        if (term == null) {
            return Collections.emptyMap();
        }
        Map<Long, Posting> postings = index.get(term.toLowerCase(Locale.ENGLISH));
        return postings != null ? Collections.unmodifiableMap(postings) : Collections.emptyMap();
    }

    /**
     * Checks if a term exists in the inverted index.
     */
    public boolean containsTerm(String term) {
        return term != null && index.containsKey(term.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Returns total number of indexed documents currently in memory.
     */
    public int getDocumentCount() {
        return docToTerms.size();
    }

    /**
     * Returns total number of distinct vocabulary terms indexed.
     */
    public int getUniqueTermCount() {
        return index.size();
    }

    /**
     * Returns total token length for a given document.
     */
    public int getDocumentLength(Long docId) {
        return docLengths.getOrDefault(docId, 0);
    }

    /**
     * Returns average document length across the entire corpus.
     */
    public double getAverageDocumentLength() {
        int docCount = getDocumentCount();
        if (docCount == 0) return 0.0;
        return (double) totalIndexedTokens.get() / docCount;
    }

    /**
     * Clears all indexed data from memory.
     */
    public synchronized void clear() {
        index.clear();
        docToTerms.clear();
        docLengths.clear();
        totalIndexedTokens.set(0);
        log.info("Inverted index cleared.");
    }
}
