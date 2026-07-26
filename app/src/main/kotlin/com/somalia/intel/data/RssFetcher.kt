package com.somalia.intel.data

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.util.concurrent.TimeUnit

class RssFetcher {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchAll(): List<NewsArticle> = withContext(Dispatchers.IO) {
        SOMALIA_SOURCES.map { source ->
            async {
                try { fetch(source) } catch (_: Exception) { emptyList() }
            }
        }.awaitAll().flatten()
            .distinctBy { it.url }
            .sortedByDescending { it.publishedAt }
    }

    private fun fetch(source: NewsSource): List<NewsArticle> {
        val req  = Request.Builder()
            .url(source.rssUrl)
            .header("User-Agent", "Mozilla/5.0 SomaliaIntel/1.0")
            .build()
        val body = http.newCall(req).execute().use { it.body?.string() ?: return emptyList() }
        return parseRss(body, source)
    }

    private fun parseRss(xml: String, source: NewsSource): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val parser   = Xml.newPullParser()
        parser.setInput(xml.reader())

        var inItem    = false
        var title     = ""
        var link      = ""
        var pubDate   = ""
        var desc      = ""
        var imageUrl: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""
            when {
                event == XmlPullParser.START_TAG && tag == "item"  -> { inItem = true }
                event == XmlPullParser.END_TAG   && tag == "item"  -> {
                    if (link.isNotBlank() && title.isNotBlank()) {
                        articles += NewsArticle(
                            title       = title.trim(),
                            summary     = desc.trim().take(300),
                            url         = link.trim(),
                            imageUrl    = imageUrl,
                            source      = source.name,
                            journalist  = source.journalist,
                            publishedAt = pubDate.trim(),
                            category    = source.category,
                        )
                    }
                    title = ""; link = ""; pubDate = ""; desc = ""; imageUrl = null; inItem = false
                }
                inItem && event == XmlPullParser.START_TAG -> when (tag) {
                    "title"   -> title   = parser.nextText()
                    "link"    -> link    = parser.nextText()
                    "pubDate" -> pubDate = parser.nextText()
                    "description" -> desc = parser.nextText().stripHtml()
                    "enclosure", "media:content", "media:thumbnail" -> {
                        if (imageUrl == null) imageUrl = parser.getAttributeValue(null, "url")
                    }
                }
            }
            event = parser.next()
        }
        return articles.take(20) // limit per source
    }

    private fun String.stripHtml() = replace(Regex("<[^>]+>"), "").trim()
}
