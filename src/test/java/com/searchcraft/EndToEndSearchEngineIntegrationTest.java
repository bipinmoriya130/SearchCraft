package com.searchcraft;

import com.searchcraft.dto.DashboardStatsDto;
import com.searchcraft.dto.SearchResponse;
import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import com.searchcraft.index.InvertedIndex;
import com.searchcraft.repository.IndexedPageRepository;
import com.searchcraft.repository.SearchHistoryRepository;
import com.searchcraft.service.IndexingService;
import com.searchcraft.service.SearchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete End-to-End System Integration Test.
 *
 * Verifies the full lifecycle of the search engine:
 * 1. Document indexing into MySQL and Inverted Index.
 * 2. Relevance ranked search execution.
 * 3. Search history recording.
 * 4. Admin stats reporting.
 * 5. Document deletion and cache/index eviction.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EndToEndSearchEngineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndexedPageRepository indexedPageRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private InvertedIndex invertedIndex;

    @Autowired
    private IndexingService indexingService;

    @Autowired
    private SearchService searchService;

    private Long docId1;
    private Long docId2;
    private Long docId3;

    @BeforeEach
    void setUp() {
        // Clear previous test records
        searchHistoryRepository.deleteAll();
        indexedPageRepository.deleteAll();
        invertedIndex.clear();

        // 1. Seed Document 1: Focus on Java & Spring Boot
        IndexedPage p1 = indexedPageRepository.save(new IndexedPage(
                "https://spring.io/guides/gs/rest-service/",
                "https://spring.io/guides/gs/rest-service/",
                "Building a RESTful Web Service with Spring Boot",
                "Learn how to create a REST service in Java using Spring Boot.",
                "Spring Boot makes building web services in Java fast and enjoyable with dependency injection.",
                200,
                IndexingStatus.INDEXED
        ));
        docId1 = p1.getId();
        invertedIndex.addDocument(p1.getId(), p1.getTitle(), p1.getDescription(), p1.getExtractedText());

        // 2. Seed Document 2: Focus on Algorithms & Data Structures
        IndexedPage p2 = indexedPageRepository.save(new IndexedPage(
                "https://en.wikipedia.org/wiki/Inverted_index",
                "https://en.wikipedia.org/wiki/Inverted_index",
                "Inverted Index Data Structure",
                "An inverted index is a database index storing a mapping from words to their document locations.",
                "In computer science, an inverted index is the core data structure used by modern search engines.",
                200,
                IndexingStatus.INDEXED
        ));
        docId2 = p2.getId();
        invertedIndex.addDocument(p2.getId(), p2.getTitle(), p2.getDescription(), p2.getExtractedText());

        // 3. Seed Document 3: Focus on MySQL Database
        IndexedPage p3 = indexedPageRepository.save(new IndexedPage(
                "https://dev.mysql.com/doc/refman/8.0/en/",
                "https://dev.mysql.com/doc/refman/8.0/en/",
                "MySQL 8.0 Reference Manual",
                "Relational database management system documentation.",
                "MySQL provides fast and secure ACID transactions, InnoDB storage engine, and SQL querying.",
                200,
                IndexingStatus.INDEXED
        ));
        docId3 = p3.getId();
        invertedIndex.addDocument(p3.getId(), p3.getTitle(), p3.getDescription(), p3.getExtractedText());
    }

    @AfterEach
    void tearDown() {
        searchHistoryRepository.deleteAll();
        indexedPageRepository.deleteAll();
        invertedIndex.clear();
    }

    @Test
    @DisplayName("End-to-End: Full Search Cycle with Relevance Ranking, Snippets, and History")
    void testEndToEndSearchWorkflow() throws Exception {
        // Step 1: Search for "spring boot rest"
        mockMvc.perform(get("/api/search")
                        .param("q", "spring boot rest")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is("spring boot rest")))
                .andExpect(jsonPath("$.totalResults", is(1)))
                .andExpect(jsonPath("$.results[0].id", is(docId1.intValue())))
                .andExpect(jsonPath("$.results[0].title", containsString("Spring Boot")))
                .andExpect(jsonPath("$.results[0].snippet", containsString("Spring Boot")))
                .andExpect(jsonPath("$.results[0].relevanceScore", greaterThan(0.0)));

        // Step 2: Search for "inverted index"
        mockMvc.perform(get("/api/search")
                        .param("q", "inverted index")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults", is(1)))
                .andExpect(jsonPath("$.results[0].id", is(docId2.intValue())))
                .andExpect(jsonPath("$.results[0].title", containsString("Inverted Index")));

        // Step 3: Verify Search History recorded both queries in MySQL
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].queryText", is("inverted index")))
                .andExpect(jsonPath("$[1].queryText", is("spring boot rest")));

        // Step 4: Verify Admin Dashboard Stats
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIndexedPages", is(3)))
                .andExpect(jsonPath("$.totalSearches", is(2)))
                .andExpect(jsonPath("$.totalUniqueTerms", greaterThan(15)));

        // Step 5: Delete Document 1 and verify Inverted Index eviction
        mockMvc.perform(delete("/api/admin/pages/" + docId1))
                .andExpect(status().isNoContent());

        assertFalse(indexedPageRepository.existsById(docId1), "Document 1 must be deleted from MySQL");

        // Re-searching for "spring boot" must now return 0 results
        mockMvc.perform(get("/api/search")
                        .param("q", "spring boot")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults", is(0)))
                .andExpect(jsonPath("$.results", hasSize(0)));
    }
}
