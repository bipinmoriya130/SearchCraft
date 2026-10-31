/**
 * SearchCraft — Centralized API Client Module
 */
const api = {
  /**
   * Performs keyword search
   */
  async search(query, page = 0, size = 10) {
    const params = new URLSearchParams({ q: query, page, size });
    const response = await fetch(`/api/search?${params.toString()}`);
    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: "Search request failed" }));
      throw new Error(err.message || "Failed to execute search");
    }
    return response.json();
  },

  /**
   * Fetches recent search history
   */
  async getHistory() {
    const response = await fetch('/api/history');
    if (!response.ok) return [];
    return response.json();
  },

  /**
   * Clears search history
   */
  async clearHistory() {
    const response = await fetch('/api/history', { method: 'DELETE' });
    if (!response.ok) throw new Error("Failed to clear search history");
  },

  /**
   * Submits a URL for indexing
   */
  async indexUrl(url) {
    const response = await fetch('/api/index', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url: url.trim() })
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || (data.details && data.details.join(', ')) || "Indexing failed");
    }
    return data;
  },

  /**
   * Retrieves paginated indexed pages for Admin
   */
  async getAdminPages(page = 0, size = 10) {
    const params = new URLSearchParams({ page, size });
    const response = await fetch(`/api/admin/pages?${params.toString()}`);
    if (!response.ok) throw new Error("Failed to load indexed pages");
    return response.json();
  },

  /**
   * Deletes an indexed page
   */
  async deletePage(id) {
    const response = await fetch(`/api/admin/pages/${id}`, { method: 'DELETE' });
    if (!response.ok) throw new Error("Failed to delete page");
  },

  /**
   * Retries indexing a failed page
   */
  async retryPage(id) {
    const response = await fetch(`/api/admin/pages/${id}/retry`, { method: 'POST' });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || "Failed to retry indexing");
    return data;
  },

  /**
   * Retrieves dashboard statistics
   */
  async getAdminStats() {
    const response = await fetch('/api/admin/stats');
    if (!response.ok) throw new Error("Failed to load stats");
    return response.json();
  }
};
