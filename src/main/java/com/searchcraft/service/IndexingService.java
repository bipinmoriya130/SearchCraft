package com.searchcraft.service;

import com.searchcraft.dto.DashboardStatsDto;
import com.searchcraft.dto.IndexedPageDto;
import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import com.searchcraft.exception.BadRequestException;
import com.searchcraft.exception.ResourceNotFoundException;
import com.searchcraft.index.InvertedIndex;
import com.searchcraft.repository.IndexedPageRepository;
import com.searchcraft.repository.SearchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service managing web page crawling, persistence, retry logic, and metadata queries.
 */
@Service
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final IndexedPageRepository indexedPageRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final WebPageFetcherService webPageFetcherService;
    private final InvertedIndex invertedIndex;

    public IndexingService(IndexedPageRepository indexedPageRepository,
                           SearchHistoryRepository searchHistoryRepository,
                           WebPageFetcherService webPageFetcherService,
                           InvertedIndex invertedIndex) {
        this.indexedPageRepository = indexedPageRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.webPageFetcherService = webPageFetcherService;
        this.invertedIndex = invertedIndex;
    }

    /**
     * Crawls, parses, and saves a URL into MySQL.
     */
    @Transactional
    public IndexedPageDto indexUrl(String url) {
        log.info("Request to index URL: {}", url);

        try {
            FetchedPageData data = webPageFetcherService.fetch(url);

            // Check if page already exists by canonical URL
            Optional<IndexedPage> existingOpt = indexedPageRepository.findByCanonicalUrl(data.canonicalUrl());

            IndexedPage page;
            if (existingOpt.isPresent()) {
                page = existingOpt.get();
                page.setUrl(data.url());
                page.setTitle(data.title());
                page.setDescription(data.description());
                page.setExtractedText(data.extractedText());
                page.setHttpStatusCode(data.httpStatusCode());
                page.setIndexingStatus(IndexingStatus.INDEXED);
                page.setLastError(null);
                page.setIndexedAt(LocalDateTime.now());
                log.info("Updated existing indexed page for canonical URL: {}", data.canonicalUrl());
            } else {
                page = new IndexedPage(
                        data.url(),
                        data.canonicalUrl(),
                        data.title(),
                        data.description(),
                        data.extractedText(),
                        data.httpStatusCode(),
                        IndexingStatus.INDEXED
                );
                log.info("Saved new indexed page for canonical URL: {}", data.canonicalUrl());
            }

            IndexedPage saved = indexedPageRepository.save(page);
            // Update in-memory inverted index immediately
            invertedIndex.addDocument(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getExtractedText()
            );
            return mapToDto(saved);

        } catch (BadRequestException e) {
            log.warn("Indexing failed for URL {}: {}", url, e.getMessage());
            // Record failure in database if not already present
            IndexedPage failedPage = indexedPageRepository.findByCanonicalUrl(url)
                    .orElse(new IndexedPage(url, url, "Failed: " + url, "", "", 0, IndexingStatus.FAILED));
            failedPage.setIndexingStatus(IndexingStatus.FAILED);
            failedPage.setLastError(e.getMessage().length() > 990 ? e.getMessage().substring(0, 990) : e.getMessage());
            failedPage.setIndexedAt(LocalDateTime.now());
            indexedPageRepository.save(failedPage);

            throw e;
        }
    }

    /**
     * Retrieves paginated list of all indexed pages for the Admin dashboard.
     */
    @Transactional(readOnly = true)
    public Page<IndexedPageDto> getIndexedPages(Pageable pageable) {
        return indexedPageRepository.findAllByOrderByIndexedAtDesc(pageable)
                .map(this::mapToDto);
    }

    /**
     * Retrieves an individual indexed page by ID.
     */
    @Transactional(readOnly = true)
    public IndexedPageDto getPageById(Long id) {
        IndexedPage page = indexedPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indexed page not found with ID: " + id));
        return mapToDto(page);
    }

    /**
     * Deletes an indexed page by ID and evicts it from the Inverted Index.
     */
    @Transactional
    public void deletePage(Long id) {
        if (!indexedPageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Page not found with ID: " + id);
        }
        indexedPageRepository.deleteById(id);
        invertedIndex.removeDocument(id);
        log.info("Deleted indexed page with ID: {}", id);
    }

    /**
     * Retries indexing an existing failed page.
     */
    @Transactional
    public IndexedPageDto retryIndexing(Long id) {
        IndexedPage existing = indexedPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));
        return indexUrl(existing.getUrl());
    }

    /**
     * Computes dashboard overview statistics.
     */
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalIndexed = indexedPageRepository.countByIndexingStatus(IndexingStatus.INDEXED);
        long totalFailed = indexedPageRepository.countByIndexingStatus(IndexingStatus.FAILED);
        long totalPending = indexedPageRepository.countByIndexingStatus(IndexingStatus.PENDING);
        long totalSearches = searchHistoryRepository.count();

        return new DashboardStatsDto(totalIndexed, totalFailed, totalPending, invertedIndex.getUniqueTermCount(), totalSearches);
    }

    public IndexedPageDto mapToDto(IndexedPage page) {
        return new IndexedPageDto(
                page.getId(),
                page.getUrl(),
                page.getCanonicalUrl(),
                page.getTitle(),
                page.getDescription(),
                page.getHttpStatusCode(),
                page.getIndexingStatus(),
                page.getLastError(),
                page.getIndexedAt()
        );
    }
}
