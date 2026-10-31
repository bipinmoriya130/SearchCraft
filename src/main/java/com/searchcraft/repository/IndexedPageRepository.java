package com.searchcraft.repository;

import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for IndexedPage entity.
 *
 * Spring automatically generates SQL implementations at runtime for query methods
 * declared in this interface.
 */
@Repository
public interface IndexedPageRepository extends JpaRepository<IndexedPage, Long> {

    /**
     * Finds a page by its canonical URL (useful to check duplicates before crawling).
     */
    Optional<IndexedPage> findByCanonicalUrl(String canonicalUrl);

    /**
     * Checks if a canonical URL already exists in the database.
     */
    boolean existsByCanonicalUrl(String canonicalUrl);

    /**
     * Retrieves all pages by their indexing status (e.g. INDEXED for rebuilding the in-memory inverted index).
     */
    List<IndexedPage> findByIndexingStatus(IndexingStatus status);

    /**
     * Retrieves paginated indexed pages ordered by the most recently indexed.
     */
    Page<IndexedPage> findAllByOrderByIndexedAtDesc(Pageable pageable);

    /**
     * Counts how many pages have a given status (used in Admin dashboard statistics).
     */
    long countByIndexingStatus(IndexingStatus status);
}
