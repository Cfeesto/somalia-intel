package com.somalia.intel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somalia.intel.data.SOMALIA_SOURCES

private val BG      = Color(0xFF0A0E1A)
private val SURFACE = Color(0xFF131929)
private val ACCENT  = Color(0xFF4FC3F7)
private val RED     = Color(0xFFEF5350)
private val AMBER   = Color(0xFFFFB300)
private val TEXT    = Color(0xFFE0E0E0)
private val MUTED   = Color(0xFF8899AA)

@Composable
fun NewsScreen(state: AppState, onFilter: (String) -> Unit, onRefresh: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val sources    = listOf("All") + SOMALIA_SOURCES.map { it.name }.distinct()

    Column(Modifier.fillMaxSize().background(BG)) {

        // ── Header ───────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0D1520)).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text("🇸🇴 SOMALIA INTEL", color = ACCENT, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${state.articles.size} headlines · live", color = MUTED, fontSize = 11.sp)
            }
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = ACCENT, strokeWidth = 2.dp)
            } else {
                TextButton(onClick = onRefresh) { Text("Refresh", color = ACCENT, fontSize = 12.sp) }
            }
        }

        // ── Source filter chips ───────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sources.forEach { src ->
                val active = src == state.activeFilter
                Surface(
                    modifier = Modifier.clickable { onFilter(src) },
                    shape    = RoundedCornerShape(20.dp),
                    color    = if (active) ACCENT.copy(alpha = 0.25f) else SURFACE,
                ) {
                    Text(
                        src,
                        modifier  = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize  = 11.sp,
                        color     = if (active) ACCENT else MUTED,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        // ── News list ─────────────────────────────────────────────────────────
        if (state.filtered.isEmpty() && !state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No articles. Pull to refresh.", color = MUTED, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            ) {
                items(state.filtered, key = { it.url }) { article ->
                    ArticleCard(article, onClick = {
                        try { uriHandler.openUri(article.url) } catch (_: Exception) {}
                    })
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: com.somalia.intel.data.NewsArticle, onClick: () -> Unit) {
    val catColor = when (article.category) {
        "security"    -> RED
        "humanitarian"-> AMBER
        "politics"    -> Color(0xFF7E57C2)
        else          -> ACCENT
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(10.dp),
        color    = SURFACE,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Category badge
                Surface(shape = RoundedCornerShape(4.dp), color = catColor.copy(alpha = 0.2f)) {
                    Text(article.category.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp, color = catColor, fontWeight = FontWeight.Bold)
                }
                // Source / journalist
                Text(
                    article.journalist?.let { "$it · ${article.source}" } ?: article.source,
                    fontSize = 10.sp, color = MUTED,
                )
            }
            Text(article.title, color = TEXT, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
            if (article.summary.isNotBlank()) {
                Text(article.summary, color = MUTED, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2)
            }
            Text(article.publishedAt.take(25), fontSize = 10.sp, color = MUTED.copy(alpha = 0.6f))
        }
    }
}
