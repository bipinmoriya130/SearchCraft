package com.searchcraft.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {

    private InvertedIndex invertedIndex;

    @BeforeEach
    void setUp() {
        Tokenizer tokenizer = new Tokenizer();
        invertedIndex = new InvertedIndex(tokenizer);
    }

    @Test
    @DisplayName("Should index documents and retrieve postings for matching terms")
    void testAddDocumentAndRetrievePostings() {
        // Document 1
        invertedIndex.addDocument(
                1L,
                "Java Programming Tutorial",
                "Learn Java fundamentals and OOP principles.",
                "Java is an object oriented programming language widely used in backend development."
        );

        // Document 2
        invertedIndex.addDocument(
                2L,
                "Spring Boot Microservices",
                "Building resilient REST APIs with Spring Boot and Java.",
                "Spring Boot makes it easy to create stand-alone production-grade Spring applications."
        );

        // Verify postings for 'java' (present in Doc 1 and Doc 2)
        Map<Long, Posting> javaPostings = invertedIndex.getPostings("java");
        assertEquals(2, javaPostings.size(), "Term 'java' should appear in both Doc 1 and Doc 2");
        assertTrue(javaPostings.containsKey(1L));
        assertTrue(javaPostings.containsKey(2L));

        // Verify title match weighting in Doc 1: "Java" is in the title, so titleFrequency > 0
        Posting doc1Posting = javaPostings.get(1L);
        assertTrue(doc1Posting.getTitleFrequency() >= 1, "Doc 1 should record title frequency for 'java'");
        assertTrue(doc1Posting.getWeightedScore() > doc1Posting.getBodyFrequency(), "Weighted score should exceed raw body frequency");

        // Verify term 'microservices' (present only in Doc 2)
        Map<Long, Posting> microservicesPostings = invertedIndex.getPostings("microservices");
        assertEquals(1, microservicesPostings.size());
        assertTrue(microservicesPostings.containsKey(2L));
        assertFalse(microservicesPostings.containsKey(1L));

        // Total documents and unique terms
        assertEquals(2, invertedIndex.getDocumentCount());
        assertTrue(invertedIndex.getUniqueTermCount() > 5);
    }

    @Test
    @DisplayName("Should remove document and prune dead terms from index in O(K) time")
    void testRemoveDocument() {
        invertedIndex.addDocument(
                10L,
                "UniqueTitleTerm",
                "Sample description",
                "Body content"
        );

        assertTrue(invertedIndex.containsTerm("uniquetitleterm"));
        assertEquals(1, invertedIndex.getDocumentCount());

        // Remove document 10
        invertedIndex.removeDocument(10L);

        assertEquals(0, invertedIndex.getDocumentCount());
        assertFalse(invertedIndex.containsTerm("uniquetitleterm"), "Dead term should be pruned from index");
        assertTrue(invertedIndex.getPostings("uniquetitleterm").isEmpty());
    }

    @Test
    @DisplayName("Should replace document postings idempotently when updated with same docId")
    void testUpdateDocument() {
        invertedIndex.addDocument(1L, "Original Title", "Original Desc", "Original Text");
        assertEquals(1, invertedIndex.getDocumentCount());
        assertTrue(invertedIndex.containsTerm("original"));

        // Update with new content
        invertedIndex.addDocument(1L, "Updated Title", "Updated Desc", "Updated Text");
        assertEquals(1, invertedIndex.getDocumentCount(), "Document count should remain 1");
        assertTrue(invertedIndex.containsTerm("updated"));
        assertFalse(invertedIndex.containsTerm("original"), "Old terms should be removed on update");
    }
}
