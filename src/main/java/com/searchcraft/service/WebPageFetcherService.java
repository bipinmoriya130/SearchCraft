package com.searchcraft.service;

import com.searchcraft.exception.BadRequestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Service responsible for safely fetching web pages over HTTP/HTTPS,
 * validating against SSRF, enforcing timeouts, and extracting clean content via Jsoup.
 */
@Service
public class WebPageFetcherService {

    private static final Logger log = LoggerFactory.getLogger(WebPageFetcherService.class);

    private final UrlSecurityValidator urlSecurityValidator;
    private final HttpClient httpClient;

    @Value("${searchcraft.crawler.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${searchcraft.crawler.read-timeout-ms:7000}")
    private int readTimeoutMs;

    @Value("${searchcraft.crawler.max-content-size-bytes:2097152}")
    private int maxContentSizeBytes;

    @Value("${searchcraft.crawler.user-agent:SearchCraft-Bot/1.0 (+http://localhost:8080/bot)}")
    private String userAgent;

    public WebPageFetcherService(UrlSecurityValidator urlSecurityValidator) {
        this.urlSecurityValidator = urlSecurityValidator;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NEVER) // Handle redirects manually to prevent redirect SSRF
                .build();
    }

    /**
     * Fetches and parses a web page safely.
     *
     * @param targetUrl The URL submitted for indexing.
     * @return FetchedPageData containing extracted title, description, text, and canonical URL.
     */
    public FetchedPageData fetch(String targetUrl) {
        // Step 1: Validate against SSRF and parse normalized URI
        URI uri = urlSecurityValidator.validateAndNormalize(targetUrl);

        int maxRedirects = 3;
        int redirectCount = 0;
        URI currentUri = uri;

        while (redirectCount <= maxRedirects) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(currentUri)
                        .timeout(Duration.ofMillis(readTimeoutMs))
                        .header("User-Agent", userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .GET()
                        .build();

                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                int statusCode = response.statusCode();

                // Handle Redirects (301, 302, 303, 307, 308)
                if (statusCode >= 300 && statusCode < 400) {
                    String location = response.headers().firstValue("Location")
                            .orElseThrow(() -> new BadRequestException("Redirect received with no Location header"));

                    URI redirectUri = currentUri.resolve(location);
                    // Validate redirect destination against SSRF!
                    currentUri = urlSecurityValidator.validateAndNormalize(redirectUri.toString());
                    redirectCount++;
                    continue;
                }

                if (statusCode != 200) {
                    throw new BadRequestException("HTTP server returned non-success status code: " + statusCode);
                }

                // Check Content-Type (ensure it's HTML, not a binary or video)
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                if (!contentType.toLowerCase().contains("text/html") && !contentType.toLowerCase().contains("xhtml")) {
                    throw new BadRequestException("Unsupported Content-Type: '" + contentType + "'. Only HTML pages can be indexed.");
                }

                // Read body with strict size limit to prevent memory exhaustion
                String html;
                try (InputStream in = response.body(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    int totalBytes = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        if (totalBytes > maxContentSizeBytes) {
                            throw new BadRequestException("Page size exceeds the maximum allowed limit of " + (maxContentSizeBytes / (1024 * 1024)) + " MB.");
                        }
                        out.write(buffer, 0, bytesRead);
                    }
                    html = out.toString(StandardCharsets.UTF_8);
                }

                // Parse and extract DOM content using Jsoup
                return extractContent(html, currentUri.toString());

            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to fetch URL: {}", currentUri, e);
                throw new BadRequestException("Failed to fetch web page: " + e.getMessage());
            }
        }

        throw new BadRequestException("Too many HTTP redirects (exceeded limit of " + maxRedirects + ").");
    }

    /**
     * Extracts title, description, body text, and canonical link using Jsoup.
     */
    public FetchedPageData extractContent(String html, String pageUrl) {
        Document doc = Jsoup.parse(html, pageUrl);

        // 1. Remove non-content elements
        doc.select("script, style, noscript, svg, iframe").remove();

        // 2. Extract Title
        String title = doc.title();
        if (title == null || title.trim().isEmpty()) {
            Element h1 = doc.selectFirst("h1");
            title = h1 != null ? h1.text() : pageUrl;
        }
        title = cleanText(title, 500);

        // 3. Extract Description
        String description = "";
        Element metaDesc = doc.selectFirst("meta[name=description], meta[property=og:description]");
        if (metaDesc != null && metaDesc.hasAttr("content")) {
            description = metaDesc.attr("content");
        } else {
            // Fallback: take first 200 characters of body text
            String bodySnippet = doc.body() != null ? doc.body().text() : "";
            description = bodySnippet.length() > 200 ? bodySnippet.substring(0, 200) + "..." : bodySnippet;
        }
        description = cleanText(description, 2000);

        // 4. Extract Canonical URL
        String canonicalUrl = pageUrl;
        Element canonicalLink = doc.selectFirst("link[rel=canonical]");
        if (canonicalLink != null && canonicalLink.hasAttr("href")) {
            String href = canonicalLink.attr("abs:href");
            if (href != null && !href.trim().isEmpty()) {
                canonicalUrl = href.trim();
            }
        }
        // Remove fragments (#...) from canonical URL
        int fragmentIndex = canonicalUrl.indexOf('#');
        if (fragmentIndex != -1) {
            canonicalUrl = canonicalUrl.substring(0, fragmentIndex);
        }

        // 5. Extract Text
        String extractedText = doc.body() != null ? doc.body().text() : "";

        return new FetchedPageData(
                pageUrl,
                canonicalUrl,
                title,
                description,
                extractedText,
                200
        );
    }

    private String cleanText(String text, int maxLength) {
        if (text == null) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }
}
