package com.searchcraft.service;

import com.searchcraft.index.InvertedIndex;
import com.searchcraft.index.Posting;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service that calculates relevance scores for candidate documents matching a search query.
 *
 * Scoring Algorithm (Field-Weighted TF-IDF with Length Normalization & Query Coordination):
 * 1. Field Weighting: Title (5.0x), Description (2.5x), Body (1.0x).
 * 2. Sublinear TF Scaling: 1 + ln(TF) to dampen keyword stuffing.
 * 3. Inverse Document Frequency (IDF): Rewards rare keywords, downweights common words.
 * 4. Document Length Normalization: Prevents very long pages from unfairly outranking concise ones.
 * 5. Query Coordination Bonus: Rewards documents that match a greater proportion of the query terms.
 */
@Service
public class RankingService {

    private final InvertedIndex invertedIndex;

    public RankingService(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    /**
     * Calculates the relevance score of a candidate document against the query tokens.
     *
     * @param documentId         The ID of the candidate document.
     * @param distinctQueryTerms The distinct normalized query tokens.
     * @return A double precision relevance score (higher is more relevant).
     */
    public double calculateRelevanceScore(Long documentId, List<String> distinctQueryTerms) {
        if (documentId == null || distinctQueryTerms == null || distinctQueryTerms.isEmpty()) {
            return 0.0;
        }

        int totalDocs = Math.max(1, invertedIndex.getDocumentCount());
        double avgDocLength = Math.max(1.0, invertedIndex.getAverageDocumentLength());
        int docLength = Math.max(1, invertedIndex.getDocumentLength(documentId));

        double rawScore = 0.0;
        int matchedTermsCount = 0;

        for (String term : distinctQueryTerms) {
            Map<Long, Posting> postings = invertedIndex.getPostings(term);
            Posting posting = postings.get(documentId);

            if (posting != null && posting.getTotalFrequency() > 0) {
                matchedTermsCount++;

                // 1. Field-weighted Term Frequency
                double weightedTf = posting.getWeightedScore();
                // Sublinear scaling: 1 + ln(TF)
                double tfScore = 1.0 + Math.log(weightedTf);

                // 2. Inverse Document Frequency (IDF)
                int docFreq = postings.size();
                double idf = Math.log(1.0 + ((totalDocs - docFreq + 0.5) / (docFreq + 0.5))) + 1.0;

                rawScore += (tfScore * idf);
            }
        }

        if (matchedTermsCount == 0) {
            return 0.0;
        }

        // 3. Document Length Normalization penalty
        double lengthNorm = 0.6 + (0.4 * (docLength / avgDocLength));
        double normalizedScore = rawScore / lengthNorm;

        // 4. Query Coordination Bonus (e.g. matching 2 of 2 words is ranked higher than matching 1 of 2)
        double coverageRatio = (double) matchedTermsCount / distinctQueryTerms.size();
        double coordinationMultiplier = 1.0 + (coverageRatio * 0.75);

        double finalScore = normalizedScore * coordinationMultiplier;

        // Round to 3 decimal places for readability
        return Math.round(finalScore * 1000.0) / 1000.0;
    }
}
