package com.somalia.intel.data

import androidx.compose.ui.graphics.Color

data class NewsSource(
    val name:       String,
    val journalist: String?,
    val rssUrl:     String,
    val color:      Long,
    val category:   String,
)

data class NewsArticle(
    val url:         String,
    val title:       String,
    val summary:     String,
    val imageUrl:    String?,
    val source:      String,
    val journalist:  String?,
    val category:    String,
    val publishedAt: String,
)

// ── Somalia Intel — verified working RSS sources (2026-07-26) ─────────────────
val SOMALIA_SOURCES = listOf(

    // ── Primary Somalia media ──────────────────────────────────────────────────
    NewsSource("Shabelle Media",   null,            "https://shabellemedia.com/feed/",                                                                   0xFF4E342E, "general"),
    NewsSource("Horseed Media",    null,            "https://horseedmedia.net/feed/",                                                                    0xFF37474F, "general"),
    NewsSource("Somaliland Standard", null,         "https://somalilandstandard.com/feed/",                                                              0xFF00695C, "politics"),

    // ── International coverage ─────────────────────────────────────────────────
    NewsSource("BBC Africa",       null,            "https://feeds.bbci.co.uk/news/world/africa/rss.xml",                                                0xFFB71C1C, "general"),
    NewsSource("Al Jazeera",       null,            "https://www.aljazeera.com/xml/rss/all.xml",                                                         0xFFE65100, "general"),

    // ── Google News topic feeds (gl=US&ceid=US:en required for reliable delivery) ──
    NewsSource("Somalia News",     null,            "https://news.google.com/rss/search?q=somalia&hl=en-US&gl=US&ceid=US:en",                            0xFF1565C0, "general"),
    NewsSource("Al-Shabaab Watch", null,            "https://news.google.com/rss/search?q=al-shabaab+somalia&hl=en-US&gl=US&ceid=US:en",                 0xFFB71C1C, "security"),
    NewsSource("Mogadishu",        null,            "https://news.google.com/rss/search?q=mogadishu&hl=en-US&gl=US&ceid=US:en",                          0xFF6A1B9A, "security"),
    NewsSource("Somalia Politics", null,            "https://news.google.com/rss/search?q=somalia+government+politics&hl=en-US&gl=US&ceid=US:en",         0xFF00695C, "politics"),
    NewsSource("Somalia Aid",      null,            "https://news.google.com/rss/search?q=somalia+humanitarian+aid+famine&hl=en-US&gl=US&ceid=US:en",     0xFF0288D1, "humanitarian"),
    NewsSource("AUSSOM/AMISOM",    null,            "https://news.google.com/rss/search?q=aussom+amisom+somalia&hl=en-US&gl=US&ceid=US:en",               0xFF4527A0, "security"),
    NewsSource("Puntland News",    null,            "https://news.google.com/rss/search?q=puntland&hl=en-US&gl=US&ceid=US:en",                           0xFF37474F, "politics"),
    NewsSource("Somaliland News",  null,            "https://news.google.com/rss/search?q=somaliland&hl=en-US&gl=US&ceid=US:en",                         0xFF2E7D32, "politics"),

    // ── Journalist feeds ───────────────────────────────────────────────────────
    NewsSource("Harun Maruf",      "Harun Maruf",   "https://news.google.com/rss/search?q=%22harun+maruf%22&hl=en-US&gl=US&ceid=US:en",                  0xFFD84315, "politics"),
    NewsSource("Abdirizak Atosh",  "Abdirizak Atosh","https://news.google.com/rss/search?q=%22abdirizak+atosh%22&hl=en-US&gl=US&ceid=US:en",             0xFFAD1457, "security"),
    NewsSource("Mohamed Salh",     "Mohamed Salh",  "https://news.google.com/rss/search?q=%22mohamed+salh%22+somalia&hl=en-US&gl=US&ceid=US:en",          0xFF558B2F, "general"),
    NewsSource("M.Y. Bakayle",     "M.Y. Bakayle",  "https://news.google.com/rss/search?q=%22bakayle%22+somalia&hl=en-US&gl=US&ceid=US:en",              0xFF6D4C41, "politics"),
    NewsSource("Ali Aadan Muumin", "Ali Aadan Muumin","https://news.google.com/rss/search?q=%22ali+aadan%22+somalia&hl=en-US&gl=US&ceid=US:en",           0xFF00838F, "security"),
    NewsSource("Idiris Alteeso",   "Idiris Alteeso","https://news.google.com/rss/search?q=%22idiris%22+somalia&hl=en-US&gl=US&ceid=US:en",                0xFF546E7A, "general"),
)
