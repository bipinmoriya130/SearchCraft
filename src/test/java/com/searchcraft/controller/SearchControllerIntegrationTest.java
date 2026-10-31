package com.searchcraft.controller;

import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import com.searchcraft.index.InvertedIndex;
import com.searchcraft.repository.IndexedPageRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndexedPageRepository indexedPageRepository;

    @Autowired
    private InvertedIndex invertedIndex;

    private IndexedPage page1;
    private IndexedPage page2;

    @BeforeEach
    void setUp() {
        // Create sample page 1
        page1 = indexedPageRepository.save(new IndexedPage(
                "https://example.com/spring-boot-guide",
                "https://example.com/spring-boot-guide",
                "Spring Boot Complete Tutorial",
                "Learn how to build production REST APIs with Spring Boot and Java.",
                "Spring Boot auto-configures Spring applications and creates standalone production grade apps.",
                200,
                IndexingStatus.INDEXED
        ));
        invertedIndex.addDocument(page1.getId(), page1.getTitle(), page1.getDescription(), page1.getExtractedText());

        // Create sample page 2
        page2 = indexedPageRepository.save(new IndexedPage(
                "https://example.com/algorithms-guide",
                "https://example.com/algorithms-guide",
                "Data Structures and Algorithms in Java",
                "Learn trees, graphs, sorting, and inverted index algorithms in Java.",
                "Algorithms form the foundation of computer science and high-speed search engines.",
                200,
                IndexingStatus.INDEXED
        ));
        invertedIndex.addDocument(page2.getId(), page2.getTitle(), page2.getDescription(), page2.getExtractedText());
    }

    @AfterEach
    void tearDown() {
        if (page1 != null && page1.getId() != null) {
            indexedPageRepository.deleteById(page1.getId());
            invertedIndex.removeDocument(page1.getId());
        }
        if (page2 != null && page2.getId() != null) {
            indexedPageRepository.deleteById(page2.getId());
            invertedIndex.removeDocument(page2.getId());
        }
    }

    @Test
    @DisplayName("GET /api/search?q=spring should return ranked results with snippets and search duration")
    void testSearchMatchingQuery() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("q", "spring boot")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is("spring boot")))
                .andExpect(jsonPath("$.totalResults", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.searchDurationMs", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.results[0].title", containsString("Spring Boot")))
                .andExpect(jsonPath("$.results[0].snippet", notNullValue()))
                .andExpect(jsonPath("$.results[0].relevanceScore", greaterThan(0.0)));
    }

    @Test
    @DisplayName("GET /api/search with empty query should return 0 results cleanly")
    void testSearchEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("q", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults", is(0)))
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/search with non-matching term should return 0 results cleanly")
    void testSearchNonMatchingQuery() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("q", "nonexistentcrypticterm123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults", is(0)))
                .andExpect(jsonPath("$.results", hasSize(0)));
    }
}
