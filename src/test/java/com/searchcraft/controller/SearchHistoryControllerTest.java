package com.searchcraft.controller;

import com.searchcraft.service.SearchHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Test
    @DisplayName("GET /api/status should return 200 OK with application info and CONNECTED database")
    void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.application", containsString("SearchCraft")))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.database", is("CONNECTED")));
    }

    @Test
    @DisplayName("GET /api/history and DELETE /api/history should operate correctly")
    void testSearchHistoryEndpoints() throws Exception {
        // Record test search
        searchHistoryService.recordSearch("data structures and algorithms", 12);

        // GET history should contain the recorded query
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].queryText", is("data structures and algorithms")))
                .andExpect(jsonPath("$[0].resultCount", is(12)));

        // DELETE history should clear records and return 204 No Content
        mockMvc.perform(delete("/api/history"))
                .andExpect(status().isNoContent());

        // Subsequent GET should be empty
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
