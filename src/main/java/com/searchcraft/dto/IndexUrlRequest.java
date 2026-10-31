package com.searchcraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for requesting a URL to be indexed.
 *
 * Enforces strict input validation:
 * - Must not be blank.
 * - Must start with http:// or https://.
 * - Must not exceed 2048 characters (RFC 7230).
 */
public class IndexUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    @Pattern(
        regexp = "^https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})?(?::[0-9]{1,5})?(?:/[^\\s]*)?$",
        message = "URL must be a valid public HTTP or HTTPS web address (e.g. https://example.com)"
    )
    private String url;

    public IndexUrlRequest() {
    }

    public IndexUrlRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
