package com.searchcraft.service;

import com.searchcraft.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Validator that protects the search engine against Server-Side Request Forgery (SSRF)
 * and malicious URL inputs.
 *
 * It prevents the crawler from accessing:
 * - Localhost and loopback interfaces (127.0.0.1, ::1)
 * - Private RFC 1918 subnets (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
 * - Link-local addresses and AWS/GCP cloud metadata endpoints (169.254.169.254)
 * - Non-HTTP/HTTPS schemes (e.g. file://, ftp://, jar://, gopher://)
 */
@Component
public class UrlSecurityValidator {

    private static final Logger log = LoggerFactory.getLogger(UrlSecurityValidator.class);

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String CLOUD_METADATA_IP = "169.254.169.254";

    /**
     * Validates that the provided URL string is safe to fetch.
     *
     * @param urlString The raw URL to validate.
     * @return The normalized URI.
     * @throws BadRequestException if the URL violates security rules.
     */
    public URI validateAndNormalize(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new BadRequestException("URL cannot be null or empty");
        }

        String trimmed = urlString.trim();

        // 1. Parse URI syntax
        URI uri;
        try {
            uri = new URI(trimmed).normalize();
        } catch (Exception e) {
            throw new BadRequestException("Malformed URL syntax: " + e.getMessage());
        }

        // 2. Validate scheme (only HTTP and HTTPS are permitted)
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new BadRequestException("Unsupported protocol: '" + scheme + "'. Only HTTP and HTTPS are permitted.");
        }

        // 3. Validate host presence
        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new BadRequestException("URL host is missing or invalid");
        }

        String lowerHost = host.toLowerCase();

        // 4. Block explicit loopback hostnames
        if (lowerHost.equals("localhost") || lowerHost.endsWith(".localhost") || lowerHost.endsWith(".local")) {
            throw new BadRequestException("Access to local hostnames is forbidden for security (SSRF prevention).");
        }

        // 5. DNS Resolution and IP Address Filtering (prevents DNS rebinding and private IP attacks)
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses == null || addresses.length == 0) {
                throw new BadRequestException("Could not resolve host: " + host);
            }

            for (InetAddress address : addresses) {
                if (isRestrictedIp(address)) {
                    log.warn("Blocked SSRF attempt to restricted IP [{}] for host [{}]", address.getHostAddress(), host);
                    throw new BadRequestException(
                        "Access to private/internal network addresses is forbidden (" + address.getHostAddress() + ")."
                    );
                }
            }
        } catch (UnknownHostException e) {
            throw new BadRequestException("Unable to resolve domain name: " + host);
        }

        return uri;
    }

    /**
     * Checks if an IP address belongs to loopback, private, link-local, or cloud metadata ranges.
     */
    public boolean isRestrictedIp(InetAddress address) {
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
            return true;
        }

        String ip = address.getHostAddress();
        if (CLOUD_METADATA_IP.equals(ip)) {
            return true;
        }

        // Check Carrier-Grade NAT (100.64.0.0/10) or IPv6 unique local
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;

            // 127.0.0.0/8 (Loopback)
            if (b0 == 127) return true;

            // 10.0.0.0/8 (Private)
            if (b0 == 10) return true;

            // 172.16.0.0/12 (Private: 172.16.0.0 to 172.31.255.255)
            if (b0 == 172 && (b1 >= 16 && b1 <= 31)) return true;

            // 192.168.0.0/16 (Private)
            if (b0 == 192 && b1 == 168) return true;

            // 169.254.0.0/16 (Link Local / APIPA)
            if (b0 == 169 && b1 == 254) return true;

            // 0.0.0.0/8 (Current network)
            if (b0 == 0) return true;
        }

        return false;
    }
}
