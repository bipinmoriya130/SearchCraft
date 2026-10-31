package com.searchcraft;

import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import com.searchcraft.entity.SearchHistory;
import com.searchcraft.repository.IndexedPageRepository;
import com.searchcraft.repository.SearchHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabasePersistenceTest {

    @Autowired
    private IndexedPageRepository indexedPageRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Test
    @DisplayName("Should successfully connect to MySQL, save an IndexedPage, and query by canonical URL")
    void testSaveAndRetrieveIndexedPage() {
        String testUrl = "https://example.com/test-page";
        String canonicalUrl = "https://example.com/test-page";

        // Clean up any previous test artifact
        indexedPageRepository.findByCanonicalUrl(canonicalUrl).ifPresent(indexedPageRepository::delete);

        IndexedPage page = new IndexedPage(
                testUrl,
                canonicalUrl,
                "Test Page Title",
                "This is a test description of the indexed page.",
                "Java Spring Boot search engine test extracted content.",
                200,
                IndexingStatus.INDEXED
        );

        IndexedPage saved = indexedPageRepository.save(page);
        assertNotNull(saved.getId(), "Database should generate an auto-increment ID");

        Optional<IndexedPage> retrieved = indexedPageRepository.findByCanonicalUrl(canonicalUrl);
        assertTrue(retrieved.isPresent(), "Page should be found in MySQL by canonical URL");
        assertEquals("Test Page Title", retrieved.get().getTitle());
        assertEquals(IndexingStatus.INDEXED, retrieved.get().getIndexingStatus());

        // Verify exists query
        assertTrue(indexedPageRepository.existsByCanonicalUrl(canonicalUrl));

        // Clean up
        indexedPageRepository.delete(saved);
        assertFalse(indexedPageRepository.existsByCanonicalUrl(canonicalUrl));
    }

    @Test
    @DisplayName("Should save and query SearchHistory in MySQL")
    void testSearchHistoryPersistence() {
        SearchHistory history = new SearchHistory("spring boot dsa", 5);
        SearchHistory saved = searchHistoryRepository.save(history);

        assertNotNull(saved.getId());
        List<SearchHistory> recent = searchHistoryRepository.findTop10ByOrderBySearchedAtDesc();
        assertFalse(recent.isEmpty(), "Recent searches should not be empty");
        assertEquals("spring boot dsa", recent.get(0).getQueryText());

        // Clean up
        searchHistoryRepository.delete(saved);
    }
}
