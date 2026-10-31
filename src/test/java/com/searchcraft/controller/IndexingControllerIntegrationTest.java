package com.searchcraft.controller;

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
class IndexingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/index should reject private loopback URLs with 400 Bad Request (SSRF block)")
    void testRejectPrivateUrlViaApi() throws Exception {
        String requestJson = """
            {
                "url": "http://127.0.0.1:8080/admin/secrets"
            }
            """;

        mockMvc.perform(post("/api/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("forbidden")));
    }

    @Test
    @DisplayName("POST /api/index should reject empty URL with 400 Validation Error")
    void testRejectBlankUrl() throws Exception {
        String requestJson = """
            {
                "url": ""
            }
            """;

        mockMvc.perform(post("/api/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }

    @Test
    @DisplayName("GET /api/admin/stats should return dashboard statistics")
    void testGetAdminStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIndexedPages", notNullValue()))
                .andExpect(jsonPath("$.totalFailedPages", notNullValue()))
                .andExpect(jsonPath("$.totalSearches", notNullValue()));
    }
}
