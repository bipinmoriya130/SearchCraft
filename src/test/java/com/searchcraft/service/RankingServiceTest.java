package com.searchcraft.service;

import com.searchcraft.index.InvertedIndex;
import com.searchcraft.index.Tokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingServiceTest {

    private InvertedIndex invertedIndex;
    private RankingService rankingService;

    @BeforeEach
    void setUp() {
        Tokenizer tokenizer = new Tokenizer();
        invertedIndex = new InvertedIndex(tokenizer);
        rankingService = new RankingService(invertedIndex);
    }

    @Test
    @DisplayName("Should rank Title matches higher than Body-only matches for the same query term")
    void testTitleMatchScoresHigherThanBodyMatch() {
        // Doc 1: "Java" is in the title
        invertedIndex.addDocument(
                1L,
                "Java Programming Masterclass",
                "Learn enterprise backend systems.",
                "General software engineering best practices and principles."
        );

        // Doc 2: "Java" is only mentioned once in the body
        invertedIndex.addDocument(
                2L,
                "Backend Engineering Overview",
                "Learn enterprise backend systems.",
                "General software engineering with some java mentions."
        );

        double scoreDoc1 = rankingService.calculateRelevanceScore(1L, List.of("java"));
        double scoreDoc2 = rankingService.calculateRelevanceScore(2L, List.of("java"));

        assertTrue(scoreDoc1 > scoreDoc2,
                "Document with term in Title (Doc 1: " + scoreDoc1 + ") should outrank term in Body only (Doc 2: " + scoreDoc2 + ")");
    }

    @Test
    @DisplayName("Should reward documents matching multiple query terms with Coordination Bonus")
    void testQueryCoordinationBonus() {
        // Doc 1: matches both "spring" and "boot"
        invertedIndex.addDocument(
                1L,
                "Spring Boot Framework",
                "Learn Spring Boot and microservices.",
                "Complete guide to building cloud applications."
        );

        // Doc 2: matches only "spring"
        invertedIndex.addDocument(
                2L,
                "Spring Framework Basics",
                "Dependency injection and inversion of control.",
                "Core container and bean lifecycle."
        );

        double scoreDoc1 = rankingService.calculateRelevanceScore(1L, List.of("spring", "boot"));
        double scoreDoc2 = rankingService.calculateRelevanceScore(2L, List.of("spring", "boot"));

        assertTrue(scoreDoc1 > scoreDoc2,
                "Document matching all query terms should outrank document matching only partial terms");
    }

    @Test
    @DisplayName("Should return 0.0 for non-matching documents")
    void testNonMatchingDocument() {
        invertedIndex.addDocument(1L, "Python Tutorial", "Learn scripting", "Data science with python");

        double score = rankingService.calculateRelevanceScore(1L, List.of("java"));
        assertEquals(0.0, score, "Non-matching document should receive 0.0 score");
    }
}
