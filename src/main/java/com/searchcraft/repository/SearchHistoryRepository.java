package com.searchcraft.repository;

import com.searchcraft.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for SearchHistory entity.
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * Retrieves the 10 most recent searches performed by users.
     */
    List<SearchHistory> findTop10ByOrderBySearchedAtDesc();

    /**
     * Retrieves the 20 most recent searches for history display.
     */
    List<SearchHistory> findTop20ByOrderBySearchedAtDesc();
}
