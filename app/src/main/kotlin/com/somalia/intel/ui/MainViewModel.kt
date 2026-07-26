package com.somalia.intel.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.somalia.intel.data.NewsArticle
import com.somalia.intel.data.RssFetcher
import com.somalia.intel.data.db.AppDatabase
import com.somalia.intel.data.preferences.AppPreferences
import com.somalia.intel.data.repository.NewsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// ── Public UI state ───────────────────────────────────────────────────────────

data class AppState(
    val articles:     List<NewsArticle> = emptyList(),
    val filtered:     List<NewsArticle> = emptyList(),
    val loading:      Boolean           = false,
    val error:        String?           = null,
    val activeFilter: String            = "All",
    val searchQuery:  String            = "",
    val aiBrief:      String            = "",
    val briefLoading: Boolean           = false,
    val claudeApiKey: String            = "",
)

// ── Private mutable UI slice ──────────────────────────────────────────────────

private data class UiSlice(
    val loading:      Boolean = false,
    val error:        String? = null,
    val activeFilter: String  = "All",
    val searchQuery:  String  = "",
    val aiBrief:      String  = "",
    val briefLoading: Boolean = false,
)

// ── Claude API models (kotlinx.serialization) ─────────────────────────────────

@Serializable private data class ClaudeRequest(
    val model: String = "claude-opus-4-6",
    val max_tokens: Int = 1024,
    val messages: List<ClaudeMsg>,
)
@Serializable private data class ClaudeMsg(val role: String, val content: String)
@Serializable private data class ClaudeResponse(val content: List<ClaudeContent>)
@Serializable private data class ClaudeContent(val type: String, val text: String)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MainViewModel(
    private val repo:  NewsRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _ui = MutableStateFlow(UiSlice())

    val state: StateFlow<AppState> = combine(
        repo.articles,
        prefs.claudeApiKey,
        prefs.cachedBrief,
        _ui,
    ) { articles, apiKey, cachedBrief, ui ->
        val filtered = articles
            .filter { a -> ui.activeFilter == "All" || a.source == ui.activeFilter || a.journalist == ui.activeFilter }
            .filter { a ->
                ui.searchQuery.isBlank() ||
                    a.title.contains(ui.searchQuery, ignoreCase = true) ||
                    a.summary.contains(ui.searchQuery, ignoreCase = true) ||
                    a.source.contains(ui.searchQuery, ignoreCase = true)
            }
        AppState(
            articles     = articles,
            filtered     = filtered,
            loading      = ui.loading,
            error        = ui.error,
            activeFilter = ui.activeFilter,
            searchQuery  = ui.searchQuery,
            aiBrief      = ui.aiBrief.ifBlank { cachedBrief },
            briefLoading = ui.briefLoading,
            claudeApiKey = apiKey,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        repo.refresh()
            .onFailure { e -> _ui.update { it.copy(error = e.message ?: "Refresh failed") } }
        _ui.update { it.copy(loading = false) }
    }

    fun setFilter(source: String) = _ui.update { it.copy(activeFilter = source) }
    fun setSearch(q: String)      = _ui.update { it.copy(searchQuery  = q) }

    fun setApiKey(key: String) = viewModelScope.launch { prefs.saveApiKey(key.trim()) }

    fun generateBrief() = viewModelScope.launch {
        val articles = state.value.articles
        val apiKey   = state.value.claudeApiKey
        _ui.update { it.copy(briefLoading = true) }
        val brief = if (apiKey.isNotBlank()) callClaude(apiKey, articles) else localBrief(articles)
        prefs.saveBrief(brief)
        _ui.update { it.copy(aiBrief = brief, briefLoading = false) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun localBrief(articles: List<NewsArticle>): String {
        val sec  = articles.count { it.category == "security" }
        val pol  = articles.count { it.category == "politics" }
        val hum  = articles.count { it.category == "humanitarian" }
        val srcs = articles.map { it.source }.distinct().size
        return buildString {
            appendLine("▌ SOMALIA INTEL SUMMARY")
            appendLine()
            appendLine("📊 ${articles.size} headlines from $srcs active sources")
            appendLine("🔴 Security: $sec  🔵 Politics: $pol  🟡 Humanitarian: $hum")
            appendLine()
            appendLine("TOP STORIES:")
            articles.take(8).forEachIndexed { i, a ->
                appendLine("${i + 1}. [${a.source}] ${a.title}")
            }
            appendLine()
            appendLine("⚠ Add Claude API key for AI-powered analysis.")
        }
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private suspend fun callClaude(key: String, articles: List<NewsArticle>): String =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val headlines = articles.take(40)
                    .joinToString("\n") { "- [${it.source}] ${it.title}" }
                val prompt = """
                    You are a Somalia intelligence analyst. Based on these headlines, produce a concise
                    situation report (max 250 words) covering:
                    1. SECURITY — active threats, Al-Shabaab, AUSSOM operations
                    2. POLITICS — government, federal/regional dynamics
                    3. HUMANITARIAN — aid, displacement, famine
                    4. KEY WATCH — one critical development to monitor

                    Use bullet points. Be direct, analytical, and objective.

                    HEADLINES:
                    $headlines
                """.trimIndent()

                val body = json.encodeToString(ClaudeRequest(messages = listOf(ClaudeMsg("user", prompt))))
                val req  = Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", key)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val resp = http.newCall(req).execute().use { it.body?.string() ?: "" }
                json.decodeFromString<ClaudeResponse>(resp).content.firstOrNull()?.text
                    ?: "No response from Claude."
            }.getOrElse { e -> "Error: ${e.message}" }
        }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db    = AppDatabase.getInstance(context.applicationContext)
                val prefs = AppPreferences(context.applicationContext)
                val repo  = NewsRepository(db, RssFetcher())
                MainViewModel(repo, prefs)
            }
        }
    }
}
