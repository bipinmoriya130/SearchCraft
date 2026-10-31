package com.searchcraft.service;

import com.searchcraft.dto.SearchHistoryDto;
import com.searchcraft.entity.SearchHistory;
import com.searchcraft.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user search history logging and retrieval.
 */
@Service
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    // Constructor injection (recommended best practice)
    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * Records a successful search query execution.
     * Trims and normalizes query before storing.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(String query, int resultCount) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String cleanedQuery = query.trim();
        if (cleanedQuery.length() > 255) {
            cleanedQuery = cleanedQuery.substring(0, 255);
        }

        SearchHistory history = new SearchHistory(cleanedQuery, resultCount);
        searchHistoryRepository.save(history);
    }

    /**
     * Retrieves the top 10 most recent searches as DTOs.
     */
    @Transactional(readOnly = true)
    public List<SearchHistoryDto> getRecentSearches() {
        return searchHistoryRepository.findTop10ByOrderBySearchedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Clears all search history records.
     */
    @Transactional
    public void clearHistory() {
        searchHistoryRepository.deleteAll();
    }

    private SearchHistoryDto mapToDto(SearchHistory entity) {
        return new SearchHistoryDto(
                entity.getId(),
                entity.getQueryText(),
                entity.getResultCount(),
                entity.getSearchedAt()
        );
    }
}
