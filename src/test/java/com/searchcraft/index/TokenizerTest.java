package com.searchcraft.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    private Tokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new Tokenizer();
    }

    @Test
    @DisplayName("Should lowercase and strip punctuation cleanly")
    void testNormalizationAndPunctuation() {
        String input = "Java, Spring-Boot! & MySQL#Database 2026?";
        List<String> tokens = tokenizer.tokenize(input);

        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("spring"));
        assertTrue(tokens.contains("boot"));
        assertTrue(tokens.contains("mysql"));
        assertTrue(tokens.contains("database"));
        assertTrue(tokens.contains("2026"));
    }

    @Test
    @DisplayName("Should eliminate stop words like 'is', 'a', 'the', 'for'")
    void testStopWordRemoval() {
        String input = "Java is a powerful language for the modern enterprise";
        List<String> tokens = tokenizer.tokenize(input);

        // Meaningful words should remain
        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("powerful"));
        assertTrue(tokens.contains("language"));
        assertTrue(tokens.contains("modern"));
        assertTrue(tokens.contains("enterprise"));

        // Stop words must be filtered out
        assertFalse(tokens.contains("is"));
        assertFalse(tokens.contains("a"));
        assertFalse(tokens.contains("for"));
        assertFalse(tokens.contains("the"));
    }

    @Test
    @DisplayName("Should accurately compute term frequencies")
    void testTermFrequencyComputation() {
        String input = "Java Spring Boot Java backend with Java";
        Map<String, Integer> tf = tokenizer.computeTermFrequencies(input);

        assertEquals(3, tf.get("java"), "Term 'java' appears 3 times");
        assertEquals(1, tf.get("spring"));
        assertEquals(1, tf.get("boot"));
        assertEquals(1, tf.get("backend"));
        assertFalse(tf.containsKey("with"), "Stop word 'with' should be excluded");
    }

    @Test
    @DisplayName("Should handle null, blank, and single-character tokens safely")
    void testEdgeCases() {
        assertTrue(tokenizer.tokenize(null).isEmpty());
        assertTrue(tokenizer.tokenize("    ").isEmpty());
        assertTrue(tokenizer.tokenize("a b c d e").isEmpty(), "Single characters should be discarded");
    }
}
