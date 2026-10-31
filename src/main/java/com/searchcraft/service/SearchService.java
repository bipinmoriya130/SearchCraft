package com.searchcraft.service;

import com.searchcraft.dto.SearchRequest;
import com.searchcraft.dto.SearchResponse;
import com.searchcraft.dto.SearchResultDto;
import com.searchcraft.entity.IndexedPage;
import com.searchcraft.index.InvertedIndex;
import com.searchcraft.index.Posting;
import com.searchcraft.index.Tokenizer;
import com.searchcraft.repository.IndexedPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core orchestrator for search query execution, candidate retrieval, ranking, and snippet generation.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final RankingService rankingService;
    private final SnippetGenerator snippetGenerator;
    private final IndexedPageRepository indexedPageRepository;
    private final SearchHistoryService searchHistoryService;

    public SearchService(Tokenizer tokenizer,
                         InvertedIndex invertedIndex,
                         RankingService rankingService,
                         SnippetGenerator snippetGenerator,
                         IndexedPageRepository indexedPageRepository,
                         SearchHistoryService searchHistoryService) {
        this.tokenizer = tokenizer;
        this.invertedIndex = invertedIndex;
        this.rankingService = rankingService;
        this.snippetGenerator = snippetGenerator;
        this.indexedPageRepository = indexedPageRepository;
        this.searchHistoryService = searchHistoryService;
    }

    /**
     * Executes a search query with pagination and relevance ranking.
     */
    @Transactional
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();

        String rawQuery = request.getQuery() != null ? request.getQuery().trim() : "";
        int page = Math.max(0, request.getPage());
        int size = Math.min(50, Math.max(1, request.getSize()));

        List<String> rawTokens = tokenizer.tokenize(rawQuery);
        List<String> queryTokens = rawTokens.stream().distinct().toList();

        // If query is empty or contained only stop words
        if (queryTokens.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            searchHistoryService.recordSearch(rawQuery, 0);
            return new SearchResponse(rawQuery, 0, duration, page, 0, Collections.emptyList());
        }

        // 1. Candidate Retrieval: Union of all document IDs containing at least one query term
        Set<Long> candidateDocIds = new HashSet<>();
        for (String token : queryTokens) {
            Map<Long, Posting> postings = invertedIndex.getPostings(token);
            candidateDocIds.addAll(postings.keySet());
        }

        if (candidateDocIds.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            searchHistoryService.recordSearch(rawQuery, 0);
            return new SearchResponse(rawQuery, 0, duration, page, 0, Collections.emptyList());
        }

        // 2. Score each candidate document using RankingService
        List<ScoredDocument> scoredDocs = new ArrayList<>(candidateDocIds.size());
        for (Long docId : candidateDocIds) {
            double score = rankingService.calculateRelevanceScore(docId, queryTokens);
            if (score > 0.0) {
                scoredDocs.add(new ScoredDocument(docId, score));
            }
        }

        // 3. Sort candidates descending by relevance score
        scoredDocs.sort((a, b) -> Double.compare(b.score, a.score));

        int totalResults = scoredDocs.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);

        // 4. Calculate pagination slice
        int fromIndex = page * size;
        if (fromIndex >= totalResults) {
            long duration = System.currentTimeMillis() - startTime;
            searchHistoryService.recordSearch(rawQuery, totalResults);
            return new SearchResponse(rawQuery, totalResults, duration, page, totalPages, Collections.emptyList());
        }

        int toIndex = Math.min(fromIndex + size, totalResults);
        List<ScoredDocument> pageSlice = scoredDocs.subList(fromIndex, toIndex);

        // 5. Batch-fetch document metadata from MySQL
        List<Long> pageDocIds = pageSlice.stream().map(d -> d.docId).toList();
        Map<Long, IndexedPage> pageEntities = indexedPageRepository.findAllById(pageDocIds)
                .stream()
                .collect(Collectors.toMap(IndexedPage::getId, p -> p));

        // 6. Build ranked SearchResultDto cards preserving score order
        List<SearchResultDto> results = new ArrayList<>(pageSlice.size());
        for (ScoredDocument scored : pageSlice) {
            IndexedPage entity = pageEntities.get(scored.docId);
            if (entity != null) {
                String snippet = snippetGenerator.generateSnippet(entity.getExtractedText(), queryTokens);
                results.add(new SearchResultDto(
                        entity.getId(),
                        entity.getUrl(),
                        entity.getTitle(),
                        snippet,
                        scored.score,
                        entity.getIndexedAt()
                ));
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Executed search for [{}] in {} ms. Returned {} results (page {} of {})",
                rawQuery, duration, totalResults, page + 1, Math.max(1, totalPages));

        // Record query in history
        searchHistoryService.recordSearch(rawQuery, totalResults);

        return new SearchResponse(rawQuery, totalResults, duration, page, totalPages, results);
    }

    private record ScoredDocument(Long docId, double score) {
    }
}
