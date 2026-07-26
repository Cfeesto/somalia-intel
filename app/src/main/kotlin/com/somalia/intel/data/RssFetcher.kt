package com.somalia.intel.data

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RssFetcher {

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) SomaliaIntel/1.0")
                    .header("Accept", "application/rss+xml,application/xml,text/xml;q=0.9,*/*;q=0.8")
                    .build()
            )
        }
        .build()

    suspend fun fetchAll(): List<NewsArticle> = withContext(Dispatchers.IO) {
        SOMALIA_SOURCES.map { source ->
            async {
                try { fetch(source) } catch (_: Exception) { emptyList() }
            }
        }
            .awaitAll()
            .flatten()
            .distinctBy { it.url }
            .sortedByDescending { it.publishedAt }
    }

    private fun fetch(source: NewsSource): List<NewsArticle> {
        val req  = Request.Builder().url(source.rssUrl).build()
        val body = http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            resp.body?.string() ?: return emptyList()
        }
        return parseRss(body, source)
    }

    private fun parseRss(xml: String, source: NewsSource): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val parser   = Xml.newPullParser()
        try { parser.setInput(xml.reader()) } catch (_: Exception) { return emptyList() }

        var inItem  = false
        var title   = ""
        var link    = ""
        var pubDate = ""
        var desc    = ""
        var imgUrl: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""
            when {
                event == XmlPullParser.START_TAG && tag == "item" -> {
                    inItem = true
                    title = ""; link = ""; pubDate = ""; desc = ""; imgUrl = null
                }
                event == XmlPullParser.END_TAG && tag == "item" -> {
                    if (link.isNotBlank() && title.isNotBlank()) {
                        articles += NewsArticle(
                            title       = title.trim().clean(),
                            summary     = desc.trim().stripHtml().clean().take(300),
                            url         = link.trim(),
                            imageUrl    = imgUrl?.takeIf { it.startsWith("http") },
                            source      = source.name,
                            journalist  = source.journalist,
                            publishedAt = pubDate.trim().relativeDate(),
                            category    = source.category,
                        )
                    }
                    inItem = false
                }
                inItem && event == XmlPullParser.START_TAG -> when (tag) {
                    "title"             -> runCatching { title   = parser.nextText() }
                    "link"              -> runCatching { link    = parser.nextText() }
                    "pubDate","dc:date","published","updated" ->
                        runCatching { if (pubDate.isBlank()) pubDate = parser.nextText() }
                    "description","content:encoded","summary" ->
                        runCatching { if (desc.isBlank()) desc = parser.nextText() }
                    "enclosure","media:content","media:thumbnail" -> {
                        if (imgUrl == null) {
                            imgUrl = parser.getAttributeValue(null, "url")
                                ?: parser.getAttributeValue(null, "href")
                        }
                    }
                }
            }
            try { event = parser.next() } catch (_: Exception) { break }
        }
        return articles.take(25)
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun String.stripHtml() = replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
    private fun String.clean() = replace("&amp;", "&").replace("&lt;", "<")
        .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ").trim()

    private val parsers = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",  Locale.ENGLISH),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",     Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",       Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH),
    )

    private fun String.relativeDate(): String {
        if (isBlank()) return ""
        val date = parsers.firstNotNullOfOrNull { runCatching { it.parse(this) }.getOrNull() }
            ?: return take(16)
        return date.toRelative()
    }

    private fun Date.toRelative(): String {
        val d = System.currentTimeMillis() - time
        return when {
            d < 60_000L       -> "just now"
            d < 3_600_000L    -> "${d / 60_000}m ago"
            d < 86_400_000L   -> "${d / 3_600_000}h ago"
            d < 604_800_000L  -> "${d / 86_400_000}d ago"
            else              -> SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(this)
        }
    }
}
