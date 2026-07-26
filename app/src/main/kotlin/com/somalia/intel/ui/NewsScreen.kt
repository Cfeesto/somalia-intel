package com.somalia.intel.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.somalia.intel.data.SOMALIA_SOURCES
import com.somalia.intel.data.NewsArticle

private val BG      = Color(0xFF0A0E1A)
private val SURFACE = Color(0xFF131929)
private val ACCENT  = Color(0xFF4FC3F7)
private val RED     = Color(0xFFEF5350)
private val AMBER   = Color(0xFFFFB300)
private val TEXT    = Color(0xFFE0E0E0)
private val MUTED   = Color(0xFF8899AA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(state: AppState, onFilter: (String) -> Unit, onSearch: (String) -> Unit, onRefresh: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val sources    = listOf("All") + SOMALIA_SOURCES.map { it.name }.distinct()

    Column(Modifier.fillMaxSize().background(BG)) {

        // ── Header ────────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0D1520)).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text("🇸🇴 SOMALIA INTEL", color = ACCENT, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${state.filtered.size} of ${state.articles.size} headlines", color = MUTED, fontSize = 11.sp)
            }
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = ACCENT, strokeWidth = 2.dp)
            }
        }

        // ── Search bar ────────────────────────────────────────────────────────
        OutlinedTextField(
            value         = state.searchQuery,
            onValueChange = onSearch,
            modifier      = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            placeholder   = { Text("Search headlines…", color = MUTED, fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = MUTED, modifier = Modifier.size(18.dp)) },
            trailingIcon  = if (state.searchQuery.isNotBlank()) {{
                IconButton(onClick = { onSearch("") }) {
                    Icon(Icons.Default.Close, null, tint = MUTED, modifier = Modifier.size(18.dp))
                }
            }} else null,
            singleLine    = true,
            shape         = RoundedCornerShape(24.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ACCENT,
                unfocusedBorderColor = Color(0xFF2A3A4A),
                focusedTextColor     = TEXT,
                unfocusedTextColor   = TEXT,
                cursorColor          = ACCENT,
                focusedContainerColor   = SURFACE,
                unfocusedContainerColor = SURFACE,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
        )

        // ── Source filter chips ───────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sources.forEach { src ->
                val active = src == state.activeFilter
                Surface(
                    modifier = Modifier.clickable { onFilter(src) },
                    shape    = RoundedCornerShape(20.dp),
                    color    = if (active) ACCENT.copy(alpha = 0.22f) else SURFACE,
                ) {
                    Text(
                        src,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize   = 11.sp,
                        color      = if (active) ACCENT else MUTED,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        // ── Pull-to-refresh + list ────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh    = onRefresh,
            modifier     = Modifier.fillMaxSize(),
        ) {
            when {
                state.loading && state.filtered.isEmpty() -> ShimmerList()
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No articles found. Pull to refresh.", color = MUTED, fontSize = 13.sp)
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    items(state.filtered, key = { it.url }) { article ->
                        ArticleCard(article) {
                            try { uriHandler.openUri(article.url) } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }
}

// ── Article card ──────────────────────────────────────────────────────────────

@Composable
private fun ArticleCard(article: NewsArticle, onClick: () -> Unit) {
    val catColor = when (article.category) {
        "security"     -> RED
        "humanitarian" -> AMBER
        "politics"     -> Color(0xFF7E57C2)
        else           -> ACCENT
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(10.dp),
        color    = SURFACE,
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            // Thumbnail
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model             = article.imageUrl,
                    contentDescription = null,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E2D3D)),
                )
            } else {
                // Category-color placeholder block
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when (article.category) {
                            "security"     -> "🔴"
                            "humanitarian" -> "🟡"
                            "politics"     -> "🔵"
                            else           -> "📰"
                        },
                        fontSize = 20.sp,
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Category badge + source
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp), color = catColor.copy(alpha = 0.2f)) {
                        Text(
                            article.category.uppercase(),
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize   = 9.sp,
                            color      = catColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        article.journalist?.let { "$it · ${article.source}" } ?: article.source,
                        fontSize = 10.sp,
                        color    = MUTED,
                        maxLines = 1,
                    )
                }
                Text(article.title, color = TEXT, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 3)
                if (article.summary.isNotBlank()) {
                    Text(article.summary, color = MUTED, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2)
                }
                Text(article.publishedAt.take(25), fontSize = 10.sp, color = MUTED.copy(alpha = 0.6f))
            }
        }
    }
}

// ── Shimmer skeleton ──────────────────────────────────────────────────────────

@Composable
private fun ShimmerList() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue   = -300f,
        targetValue    = 1000f,
        animationSpec  = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label          = "shimmerOffset",
    )
    val shimmerBrush = Brush.linearGradient(
        colors      = listOf(Color(0xFF1E2D3D), Color(0xFF2A3E52), Color(0xFF1E2D3D)),
        start       = Offset(offset, 0f),
        end         = Offset(offset + 600f, 300f),
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        userScrollEnabled   = false,
    ) {
        items(6) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = SURFACE) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(CircleShape).background(shimmerBrush))
                        Box(Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(shimmerBrush))
                        Box(Modifier.fillMaxWidth(0.8f).height(14.dp).clip(CircleShape).background(shimmerBrush))
                        Box(Modifier.fillMaxWidth(0.5f).height(10.dp).clip(CircleShape).background(shimmerBrush))
                    }
                }
            }
        }
    }
}
