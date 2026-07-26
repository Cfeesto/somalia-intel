package com.somalia.intel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somalia.intel.data.NewsArticle
import com.somalia.intel.data.RssFetcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AppState(
    val articles:       List<NewsArticle> = emptyList(),
    val filtered:       List<NewsArticle> = emptyList(),
    val loading:        Boolean           = false,
    val activeFilter:   String            = "All",    // source name or "All"
    val aiBrief:        String            = "",
    val briefLoading:   Boolean           = false,
    val claudeApiKey:   String            = "",
)

class MainViewModel : ViewModel() {

    private val fetcher = RssFetcher()
    private val _state  = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    init {
        refresh()
        // auto-refresh every 15 minutes
        viewModelScope.launch {
            while (true) {
                delay(15 * 60 * 1000L)
                refresh()
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        val articles = fetcher.fetchAll()
        _state.value = _state.value.copy(
            articles  = articles,
            filtered  = applyFilter(articles, _state.value.activeFilter),
            loading   = false,
        )
    }

    fun setFilter(source: String) {
        _state.value = _state.value.copy(
            activeFilter = source,
            filtered     = applyFilter(_state.value.articles, source),
        )
    }

    fun setApiKey(key: String) {
        _state.value = _state.value.copy(claudeApiKey = key)
    }

    fun generateBrief() = viewModelScope.launch {
        val key = _state.value.claudeApiKey
        if (key.isBlank()) {
            _state.value = _state.value.copy(aiBrief = buildLocalBrief())
            return@launch
        }
        _state.value = _state.value.copy(briefLoading = true)
        val brief = callClaude(key, buildContext())
        _state.value = _state.value.copy(aiBrief = brief, briefLoading = false)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun applyFilter(all: List<NewsArticle>, filter: String) =
        if (filter == "All") all else all.filter { it.source == filter || it.journalist == filter }

    private fun buildContext(): String {
        val top = _state.value.articles.take(30)
        return "Somalia intelligence brief. Latest headlines:\n" +
            top.joinToString("\n") { "- [${it.source}] ${it.title}" }
    }

    private fun buildLocalBrief(): String {
        val articles = _state.value.articles
        val security = articles.count { it.category == "security" }
        val total    = articles.size
        val sources  = articles.map { it.source }.distinct().take(5).joinToString(", ")
        return "📊 Somalia Intelligence Summary\n\n" +
            "Total headlines: $total\n" +
            "Security incidents: $security\n" +
            "Active sources: $sources\n\n" +
            "Top stories:\n" +
            articles.take(5).joinToString("\n") { "• ${it.title} (${it.source})" } +
            "\n\n⚠️ Add Claude API key in Settings for AI-powered analysis."
    }

    private fun callClaude(key: String, context: String): String {
        return try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val body = """{"model":"claude-opus-4-6","max_tokens":1024,"messages":[{"role":"user","content":${jsonStr("You are a Somalia intelligence analyst. Based on these headlines, write a concise 200-word situation report covering: security threats, political developments, humanitarian situation. Use bullet points.\n\n$context")}}]}"""
            val req = okhttp3.Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .header("x-api-key", key)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), body))
                .build()
            val resp = client.newCall(req).execute().use { it.body?.string() ?: "" }
            org.json.JSONObject(resp)
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Failed to generate brief: ${e.message}"
        }
    }

    private fun jsonStr(s: String) = "\"${s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
}
