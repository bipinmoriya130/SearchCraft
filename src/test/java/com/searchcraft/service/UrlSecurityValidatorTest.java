package com.searchcraft.service;

import com.searchcraft.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UrlSecurityValidatorTest {

    private UrlSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlSecurityValidator();
    }

    @Test
    @DisplayName("Should accept valid public HTTP/HTTPS URLs")
    void testValidPublicUrls() {
        URI uri = validator.validateAndNormalize("https://example.com/page?query=test");
        assertNotNull(uri);
        assertEquals("example.com", uri.getHost());
        assertEquals("https", uri.getScheme());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:8080/api",
        "http://localhost/secret",
        "https://sub.localhost/admin",
        "http://127.0.0.1/admin",
        "http://127.0.0.2:9000",
        "http://10.0.0.1/internal",
        "http://192.168.1.1/router",
        "http://172.16.0.1/dashboard",
        "http://169.254.169.254/latest/meta-data/",
        "file:///etc/passwd",
        "ftp://internal.server/file.txt",
        "gopher://example.com",
        "javascript:alert(1)"
    })
    @DisplayName("Should reject private IPs, loopbacks, cloud metadata, and dangerous protocols (SSRF Defense)")
    void testRejectRestrictedUrls(String maliciousUrl) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            validator.validateAndNormalize(maliciousUrl);
        });
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null or empty URL")
    void testRejectEmptyUrl() {
        assertThrows(BadRequestException.class, () -> validator.validateAndNormalize("   "));
        assertThrows(BadRequestException.class, () -> validator.validateAndNormalize(null));
    }
}
