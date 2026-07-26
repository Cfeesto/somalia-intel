package com.somalia.intel.data

data class NewsArticle(
    val title:       String,
    val summary:     String,
    val url:         String,
    val imageUrl:    String?,
    val source:      String,   // outlet name
    val journalist:  String?,  // journalist name if known
    val publishedAt: String,
    val category:    String,   // security | politics | humanitarian | general
)

data class NewsSource(
    val name:       String,
    val journalist: String?,
    val rssUrl:     String,
    val color:      Long,      // ARGB for UI badge
    val category:   String,
)

// ── Verified Somalia sources ─────────────────────────────────────────────────
val SOMALIA_SOURCES = listOf(
    // ── TV / Media outlets ───────────────────────────────────────────────────
    NewsSource("Dalsan TV",      null,              "https://dalsan.net/feed/",                                          0xFF1565C0, "general"),
    NewsSource("Dawan TV",       null,              "https://news.google.com/rss/search?q=dawan+tv+somalia&hl=en",       0xFF6A1B9A, "general"),
    NewsSource("Garowe Online",  null,              "https://www.garoweonline.com/en/rss",                               0xFF00695C, "politics"),
    NewsSource("Shabelle Media", null,              "https://shabellemedia.com/feed/",                                   0xFF4E342E, "general"),
    NewsSource("Radio Dalsan",   null,              "https://dalsan.net/feed/",                                          0xFF1565C0, "general"),
    NewsSource("VOA Somalia",    "Harun Maruf",     "https://www.voanews.com/api/zv_omqympqit",                          0xFF1976D2, "politics"),
    NewsSource("UN OCHA",        null,              "https://reliefweb.int/country/som/rss.xml",                         0xFF0288D1, "humanitarian"),

    // ── Journalist-specific Google News RSS ─────────────────────────────────
    NewsSource("Harun Maruf",    "Harun Maruf",     "https://news.google.com/rss/search?q=%22harun+maruf%22+somalia&hl=en", 0xFFD84315, "politics"),
    NewsSource("Abdirizak Atosh","Abdirizak Atosh", "https://news.google.com/rss/search?q=%22abdirizak+atosh%22&hl=en",     0xFFAD1457, "security"),
    NewsSource("Mohamed Salh",   "Mohamed Salh",    "https://news.google.com/rss/search?q=%22mohamed+salh%22+somalia&hl=en",0xFF558B2F, "general"),
    NewsSource("Bakayle",        "Mohamed Y. Bakayle","https://news.google.com/rss/search?q=%22bakayle%22+somalia&hl=en",  0xFF6D4C41, "politics"),
    NewsSource("Ali Aadan Muumin","Ali Aadan Muumin","https://news.google.com/rss/search?q=%22ali+aadan+muumin%22&hl=en", 0xFF00838F, "security"),
    NewsSource("Idiris Alteeso", "Idiris Alteeso",  "https://news.google.com/rss/search?q=%22idiris+alteeso%22+somalia&hl=en",0xFF37474F,"general"),

    // ── International Somalia coverage ───────────────────────────────────────
    NewsSource("BBC Africa",     null,              "https://feeds.bbci.co.uk/news/world/africa/rss.xml",                0xFFB71C1C, "general"),
    NewsSource("Al Jazeera",     null,              "https://www.aljazeera.com/xml/rss/all.xml",                         0xFFE65100, "general"),
    NewsSource("Reuters Somalia","null",            "https://news.google.com/rss/search?q=somalia+site:reuters.com&hl=en",0xFF263238,"general"),
    NewsSource("Al-Shabaab",     null,              "https://news.google.com/rss/search?q=al-shabaab+somalia&hl=en",     0xFFB71C1C, "security"),
)
