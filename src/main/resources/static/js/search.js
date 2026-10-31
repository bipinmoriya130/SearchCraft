/**
 * SearchCraft — Search Client Script (Home & Results Pages)
 */

/**
 * Initializes the Search Home Page logic
 */
function initSearchHome() {
  const searchInput = document.getElementById('searchInput');
  const clearBtn = document.getElementById('clearBtn');
  const searchForm = document.getElementById('searchForm');
  const submitSearchBtn = document.getElementById('submitSearchBtn');
  const curiousBtn = document.getElementById('curiousBtn');
  const dropdown = document.getElementById('suggestionsDropdown');

  if (!searchInput) return;

  // Toggle clear button
  searchInput.addEventListener('input', () => {
    clearBtn.style.display = searchInput.value.trim() ? 'block' : 'none';
  });

  clearBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearBtn.style.display = 'none';
    dropdown.style.display = 'none';
    searchInput.focus();
  });

  submitSearchBtn.addEventListener('click', () => {
    if (searchInput.value.trim()) {
      searchForm.submit();
    }
  });

  // "I'm Feeling Curious" button picks a sample query
  const sampleTopics = ['Java', 'Spring Boot', 'Inverted Index', 'Data Structures', 'REST API', 'MySQL'];
  curiousBtn.addEventListener('click', () => {
    const randomTopic = sampleTopics[Math.floor(Math.random() * sampleTopics.length)];
    window.location.href = `/results.html?q=${encodeURIComponent(randomTopic)}`;
  });

  // Fetch search history for autosuggest
  api.getHistory().then(historyItems => {
    if (!historyItems || historyItems.length === 0) return;

    searchInput.addEventListener('focus', () => {
      showSuggestions(historyItems);
    });

    searchInput.addEventListener('input', () => {
      const val = searchInput.value.toLowerCase().trim();
      const filtered = historyItems.filter(item => 
        item.queryText.toLowerCase().includes(val)
      );
      showSuggestions(filtered);
    });
  }).catch(() => {});

  function showSuggestions(items) {
    if (!items || items.length === 0) {
      dropdown.style.display = 'none';
      return;
    }

    dropdown.innerHTML = items.slice(0, 6).map(item => `
      <div class="suggestion-item" data-query="${escapeHtml(item.queryText)}">
        <span>🕒</span>
        <span>${escapeHtml(item.queryText)}</span>
      </div>
    `).join('');

    dropdown.style.display = 'block';

    dropdown.querySelectorAll('.suggestion-item').forEach(el => {
      el.addEventListener('mousedown', (e) => {
        e.preventDefault();
        const query = el.getAttribute('data-query');
        searchInput.value = query;
        dropdown.style.display = 'none';
        searchForm.submit();
      });
    });
  }

  // Hide suggestions when clicking outside
  document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !dropdown.contains(e.target)) {
      dropdown.style.display = 'none';
    }
  });
}

/**
 * Initializes the Search Results Page logic
 */
function initSearchResults() {
  const urlParams = new URLSearchParams(window.location.search);
  const query = urlParams.get('q') || '';
  const page = parseInt(urlParams.get('page') || '0', 10);
  const pageSize = 10;

  const resultsInput = document.getElementById('resultsSearchInput');
  const loadingState = document.getElementById('loadingState');
  const errorBanner = document.getElementById('errorBanner');
  const resultsList = document.getElementById('resultsList');
  const emptyState = document.getElementById('emptyState');
  const metaBar = document.getElementById('metaBar');
  const metaText = document.getElementById('resultsMetaText');
  const paginationControls = document.getElementById('paginationControls');
  const prevPageBtn = document.getElementById('prevPageBtn');
  const nextPageBtn = document.getElementById('nextPageBtn');
  const pageInfoText = document.getElementById('pageInfoText');

  if (resultsInput) {
    resultsInput.value = query;
  }

  if (!query.trim()) {
    loadingState.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }

  // Execute API Search
  api.search(query, page, pageSize)
    .then(data => {
      loadingState.style.display = 'none';
      metaBar.style.display = 'block';
      metaText.textContent = `About ${data.totalResults} results (${data.searchDurationMs} ms) for "${query}"`;

      if (!data.results || data.results.length === 0) {
        emptyState.style.display = 'block';
        return;
      }

      // Render search result cards
      resultsList.innerHTML = data.results.map(result => `
        <article class="result-card">
          <div class="result-url">${escapeHtml(result.url)}</div>
          <h2 class="result-title">
            <a href="${escapeHtml(result.url)}" target="_blank" rel="noopener noreferrer">
              ${escapeHtml(result.title || result.url)}
            </a>
          </h2>
          <p class="result-snippet">${escapeHtml(result.snippet || 'No preview available')}</p>
          <div class="result-footer">
            <span class="score-badge">Relevance: ${result.relevanceScore.toFixed(2)}</span>
            <span>Indexed: ${formatDate(result.indexedAt)}</span>
          </div>
        </article>
      `).join('');

      // Setup Pagination
      if (data.totalPages > 1) {
        paginationControls.style.display = 'flex';
        pageInfoText.textContent = `Page ${data.currentPage + 1} of ${data.totalPages}`;

        prevPageBtn.disabled = data.currentPage <= 0;
        nextPageBtn.disabled = data.currentPage >= data.totalPages - 1;

        prevPageBtn.onclick = () => {
          if (data.currentPage > 0) {
            window.location.href = `/results.html?q=${encodeURIComponent(query)}&page=${data.currentPage - 1}`;
          }
        };

        nextPageBtn.onclick = () => {
          if (data.currentPage < data.totalPages - 1) {
            window.location.href = `/results.html?q=${encodeURIComponent(query)}&page=${data.currentPage + 1}`;
          }
        };
      }
    })
    .catch(err => {
      loadingState.style.display = 'none';
      errorBanner.textContent = err.message || "Failed to load search results.";
      errorBanner.style.display = 'block';
    });
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function formatDate(dateString) {
  if (!dateString) return 'Recent';
  try {
    const d = new Date(dateString);
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
  } catch (e) {
    return dateString;
  }
}
