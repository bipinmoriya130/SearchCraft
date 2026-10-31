/**
 * SearchCraft — Admin Dashboard Script
 */

let currentAdminPage = 0;
const adminPageSize = 10;

function initAdminDashboard() {
  loadStats();
  loadPagesTable(currentAdminPage);
  loadHistoryTable();

  // Refresh Stats Button
  const refreshBtn = document.getElementById('refreshStatsBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadStats();
      loadPagesTable(currentAdminPage);
      loadHistoryTable();
      showSuccess("Dashboard data refreshed.");
    });
  }

  // Index New URL Form
  const indexForm = document.getElementById('indexUrlForm');
  const urlInput = document.getElementById('urlInput');
  const submitBtn = document.getElementById('indexSubmitBtn');
  const btnSpinner = document.getElementById('indexBtnSpinner');
  const btnText = document.getElementById('indexBtnText');

  if (indexForm) {
    indexForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const targetUrl = urlInput.value.trim();
      if (!targetUrl) return;

      // Loading state
      submitBtn.disabled = true;
      btnSpinner.style.display = 'inline-block';
      btnText.textContent = 'Crawling & Indexing...';
      hideAlerts();

      try {
        const page = await api.indexUrl(targetUrl);
        showSuccess(`Successfully crawled and indexed: "${page.title || page.url}"`);
        urlInput.value = '';
        loadStats();
        loadPagesTable(currentAdminPage);
      } catch (err) {
        showError(err.message || "Failed to index URL. Please check server logs.");
        loadStats();
        loadPagesTable(currentAdminPage);
      } finally {
        submitBtn.disabled = false;
        btnSpinner.style.display = 'none';
        btnText.textContent = 'Fetch & Index URL';
      }
    });
  }

  // Clear History Button
  const clearHistoryBtn = document.getElementById('clearHistoryBtn');
  if (clearHistoryBtn) {
    clearHistoryBtn.addEventListener('click', async () => {
      if (!confirm("Are you sure you want to clear all user search history?")) {
        return;
      }
      try {
        await api.clearHistory();
        showSuccess("Search history cleared.");
        loadStats();
        loadHistoryTable();
      } catch (err) {
        showError(err.message || "Failed to clear search history.");
      }
    });
  }

  // Pagination Buttons
  const prevBtn = document.getElementById('adminPrevBtn');
  const nextBtn = document.getElementById('adminNextBtn');
  if (prevBtn && nextBtn) {
    prevBtn.addEventListener('click', () => {
      if (currentAdminPage > 0) {
        currentAdminPage--;
        loadPagesTable(currentAdminPage);
      }
    });
    nextBtn.addEventListener('click', () => {
      currentAdminPage++;
      loadPagesTable(currentAdminPage);
    });
  }
}

async function loadStats() {
  try {
    const stats = await api.getAdminStats();
    document.getElementById('statIndexedPages').textContent = stats.totalIndexedPages;
    document.getElementById('statUniqueTerms').textContent = stats.totalUniqueTerms;
    document.getElementById('statTotalSearches').textContent = stats.totalSearches;
    document.getElementById('statFailedPages').textContent = stats.totalFailedPages;
  } catch (e) {
    console.error("Failed to load admin stats", e);
  }
}

async function loadPagesTable(page) {
  const tbody = document.getElementById('pagesTableBody');
  const countBadge = document.getElementById('pageCountBadge');
  const pageInfo = document.getElementById('adminPageInfo');
  const prevBtn = document.getElementById('adminPrevBtn');
  const nextBtn = document.getElementById('adminNextBtn');

  try {
    const data = await api.getAdminPages(page, adminPageSize);
    countBadge.textContent = `Total: ${data.totalElements} documents`;
    pageInfo.textContent = `Page ${data.number + 1} of ${Math.max(1, data.totalPages)}`;

    prevBtn.disabled = data.first;
    nextBtn.disabled = data.last;

    if (!data.content || data.content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No documents indexed yet. Use the form above to index your first web page!</td></tr>`;
      return;
    }

    tbody.innerHTML = data.content.map(p => `
      <tr>
        <td style="font-weight: 600; color: var(--text-muted);">${p.id}</td>
        <td>
          <div style="font-weight: 600; margin-bottom: 0.2rem;">
            <a href="${escapeHtml(p.url)}" target="_blank" rel="noopener noreferrer" style="color: var(--primary); text-decoration: none;">
              ${escapeHtml(p.title || 'Untitled Document')}
            </a>
          </div>
          <div style="font-size: 0.8rem; color: var(--text-muted); word-break: break-all;">
            ${escapeHtml(p.canonicalUrl)}
          </div>
          ${p.lastError ? `<div style="font-size: 0.78rem; color: var(--danger); margin-top: 0.25rem;">⚠️ Error: ${escapeHtml(p.lastError)}</div>` : ''}
        </td>
        <td>
          <span class="status-badge status-${p.indexingStatus}">${p.indexingStatus}</span>
        </td>
        <td style="font-size: 0.85rem; color: var(--text-muted);">
          ${formatDateTime(p.indexedAt)}
        </td>
        <td style="text-align: right;">
          ${p.indexingStatus === 'FAILED' ? `
            <button class="btn btn-secondary btn-sm" onclick="retryPage(${p.id})" style="margin-right: 0.35rem;" title="Retry Indexing">↻</button>
          ` : ''}
          <button class="btn btn-danger btn-sm" onclick="deletePage(${p.id})" title="Delete Document">Delete</button>
        </td>
      </tr>
    `).join('');

  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger); padding: 1.5rem;">Failed to load pages: ${escapeHtml(err.message)}</td></tr>`;
  }
}

async function loadHistoryTable() {
  const tbody = document.getElementById('historyTableBody');
  try {
    const history = await api.getHistory();
    if (!history || history.length === 0) {
      tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No recent searches recorded.</td></tr>`;
      return;
    }

    tbody.innerHTML = history.map(h => `
      <tr>
        <td style="font-weight: 500;">
          <a href="/results.html?q=${encodeURIComponent(h.queryText)}" style="color: var(--primary); text-decoration: none;">
            🔍 ${escapeHtml(h.queryText)}
          </a>
        </td>
        <td>${h.resultCount} results</td>
        <td style="font-size: 0.85rem; color: var(--text-muted);">${formatDateTime(h.searchedAt)}</td>
      </tr>
    `).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--danger);">Failed to load search history.</td></tr>`;
  }
}

async function deletePage(id) {
  if (!confirm(`Are you sure you want to delete document ID #${id}? It will be removed from MySQL and evicted from the Inverted Index.`)) {
    return;
  }
  try {
    await api.deletePage(id);
    showSuccess(`Deleted document ID #${id}`);
    loadStats();
    loadPagesTable(currentAdminPage);
  } catch (err) {
    showError(err.message || "Failed to delete page.");
  }
}

async function retryPage(id) {
  try {
    showSuccess(`Retrying indexing for document ID #${id}...`);
    await api.retryPage(id);
    showSuccess(`Document ID #${id} successfully indexed!`);
    loadStats();
    loadPagesTable(currentAdminPage);
  } catch (err) {
    showError(err.message || "Retry failed.");
  }
}

function showSuccess(msg) {
  hideAlerts();
  const el = document.getElementById('adminAlertSuccess');
  el.textContent = msg;
  el.style.display = 'block';
  setTimeout(() => { el.style.display = 'none'; }, 5000);
}

function showError(msg) {
  hideAlerts();
  const el = document.getElementById('adminAlertDanger');
  el.textContent = msg;
  el.style.display = 'block';
}

function hideAlerts() {
  document.getElementById('adminAlertSuccess').style.display = 'none';
  document.getElementById('adminAlertDanger').style.display = 'none';
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function formatDateTime(dateStr) {
  if (!dateStr) return 'N/A';
  try {
    const d = new Date(dateStr);
    return d.toLocaleString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}
