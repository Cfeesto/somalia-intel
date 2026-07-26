package com.somalia.intel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somalia.intel.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SomaliaIntelApp() }
    }
}

@Composable
fun SomaliaIntelApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab   by remember { mutableIntStateOf(0) }

    Surface(color = Color(0xFF0A0E1A), modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFF0A0E1A),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0D1520), contentColor = Color(0xFF4FC3F7)) {
                    NavigationBarItem(
                        selected  = tab == 0,
                        onClick   = { tab = 0 },
                        icon      = { Icon(Icons.Default.Article, null) },
                        label     = { Text("News") },
                        colors    = navColors(),
                    )
                    NavigationBarItem(
                        selected  = tab == 1,
                        onClick   = { tab = 1 },
                        icon      = { Icon(Icons.Default.Map, null) },
                        label     = { Text("Map") },
                        colors    = navColors(),
                    )
                    NavigationBarItem(
                        selected  = tab == 2,
                        onClick   = { tab = 2 },
                        icon      = { Icon(Icons.Default.Psychology, null) },
                        label     = { Text("Brief") },
                        colors    = navColors(),
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    0 -> NewsScreen(state, onFilter = vm::setFilter, onRefresh = vm::refresh)
                    1 -> MapScreen()
                    2 -> BriefScreen(state, onGenerate = vm::generateBrief, onSaveKey = vm::setApiKey)
                }
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = Color(0xFF4FC3F7),
    selectedTextColor   = Color(0xFF4FC3F7),
    unselectedIconColor = Color(0xFF8899AA),
    unselectedTextColor = Color(0xFF8899AA),
    indicatorColor      = Color(0xFF4FC3F7).copy(alpha = 0.15f),
)
