package com.searchcraft.config;

import com.searchcraft.entity.IndexedPage;
import com.searchcraft.entity.IndexingStatus;
import com.searchcraft.index.InvertedIndex;
import com.searchcraft.repository.IndexedPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bootstrap runner that rebuilds the in-memory inverted index from MySQL upon application startup.
 *
 * Guarantees data durability across application restarts without requiring external index services.
 */
@Component
public class IndexInitializationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializationRunner.class);

    private final IndexedPageRepository indexedPageRepository;
    private final InvertedIndex invertedIndex;

    public IndexInitializationRunner(IndexedPageRepository indexedPageRepository, InvertedIndex invertedIndex) {
        this.indexedPageRepository = indexedPageRepository;
        this.invertedIndex = invertedIndex;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Restoring Inverted Index from MySQL database...");

        List<IndexedPage> indexedPages = indexedPageRepository.findByIndexingStatus(IndexingStatus.INDEXED);

        for (IndexedPage page : indexedPages) {
            invertedIndex.addDocument(
                    page.getId(),
                    page.getTitle(),
                    page.getDescription(),
                    page.getExtractedText()
            );
        }

        log.info("Inverted Index rebuilt successfully! Restored {} documents with {} unique terms into memory.",
                invertedIndex.getDocumentCount(),
                invertedIndex.getUniqueTermCount());
    }
}
