package com.searchcraft.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebPageFetcherServiceTest {

    private WebPageFetcherService fetcherService;

    @BeforeEach
    void setUp() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        fetcherService = new WebPageFetcherService(validator);
    }

    @Test
    @DisplayName("Should accurately parse title, meta description, canonical URL, and body text from HTML")
    void testExtractContentFromHtml() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Java Backend Development Guide</title>
                <meta name="description" content="Learn Spring Boot, Hibernate, and REST APIs.">
                <link rel="canonical" href="https://example.com/java-guide">
                <style>body { font-family: sans-serif; }</style>
                <script>console.log('Ignore script');</script>
            </head>
            <body>
                <header><nav><a href="/">Home</a></nav></header>
                <main>
                    <h1>Mastering Java Backend</h1>
                    <p>Spring Boot simplifies building enterprise-grade REST APIs in Java.</p>
                </main>
                <noscript>JavaScript disabled</noscript>
            </body>
            </html>
            """;

        FetchedPageData data = fetcherService.extractContent(html, "https://example.com/java-guide?utm_source=twitter");

        assertNotNull(data);
        assertEquals("Java Backend Development Guide", data.title());
        assertEquals("Learn Spring Boot, Hibernate, and REST APIs.", data.description());
        assertEquals("https://example.com/java-guide", data.canonicalUrl(), "Should extract canonical URL from link tag");
        assertFalse(data.extractedText().contains("console.log"), "Scripts must be stripped");
        assertFalse(data.extractedText().contains("font-family"), "Styles must be stripped");
        assertTrue(data.extractedText().contains("Mastering Java Backend"));
        assertTrue(data.extractedText().contains("Spring Boot simplifies building enterprise-grade REST APIs in Java."));
    }
}
