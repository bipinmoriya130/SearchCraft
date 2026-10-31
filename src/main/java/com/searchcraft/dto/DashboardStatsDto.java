package com.searchcraft.dto;

/**
 * Data Transfer Object containing system overview metrics for the Admin Dashboard.
 */
public class DashboardStatsDto {

    private long totalIndexedPages;
    private long totalFailedPages;
    private long totalPendingPages;
    private int totalUniqueTerms;
    private long totalSearches;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalIndexedPages, long totalFailedPages, long totalPendingPages,
                             int totalUniqueTerms, long totalSearches) {
        this.totalIndexedPages = totalIndexedPages;
        this.totalFailedPages = totalFailedPages;
        this.totalPendingPages = totalPendingPages;
        this.totalUniqueTerms = totalUniqueTerms;
        this.totalSearches = totalSearches;
    }

    public long getTotalIndexedPages() {
        return totalIndexedPages;
    }

    public void setTotalIndexedPages(long totalIndexedPages) {
        this.totalIndexedPages = totalIndexedPages;
    }

    public long getTotalFailedPages() {
        return totalFailedPages;
    }

    public void setTotalFailedPages(long totalFailedPages) {
        this.totalFailedPages = totalFailedPages;
    }

    public long getTotalPendingPages() {
        return totalPendingPages;
    }

    public void setTotalPendingPages(long totalPendingPages) {
        this.totalPendingPages = totalPendingPages;
    }

    public int getTotalUniqueTerms() {
        return totalUniqueTerms;
    }

    public void setTotalUniqueTerms(int totalUniqueTerms) {
        this.totalUniqueTerms = totalUniqueTerms;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }
}
