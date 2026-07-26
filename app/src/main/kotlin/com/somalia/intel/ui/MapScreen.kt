package com.somalia.intel.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen() {
    Column(Modifier.fillMaxSize().background(Color(0xFF0A0E1A))) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0D1520)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🗺️  SOMALIA THREAT MAP", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess   = true
                    webChromeClient = WebChromeClient()
                    webViewClient   = WebViewClient()
                    // ponytail: loadDataWithBaseURL lets relative asset:// refs resolve correctly; no CDN needed
                    val html = ctx.assets.open("map.html").bufferedReader().use { it.readText() }
                    loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
