package com.somalia.intel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BriefScreen(state: AppState, onGenerate: () -> Unit, onSaveKey: (String) -> Unit) {
    // Only pre-fill if user hasn't entered anything; show placeholder when key is saved
    var keyInput by remember { mutableStateOf("") }
    val savedKeyHint = if (state.claudeApiKey.isNotBlank())
        "Key saved (${state.claudeApiKey.take(10)}…)" else ""

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0A0E1A)).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0D1520)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🧠  AI INTELLIGENCE BRIEF", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // API key card
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131929))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Claude API Key", color = Color(0xFF4FC3F7), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (savedKeyHint.isNotBlank()) {
                            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = Color(0xFF0D47A1).copy(alpha = 0.4f)) {
                                Text(savedKeyHint, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp, color = Color(0xFF4FC3F7))
                            }
                        }
                    }
                    Text("Required for AI-powered analysis.", color = Color(0xFF8899AA), fontSize = 11.sp)
                    OutlinedTextField(
                        value                  = keyInput,
                        onValueChange          = { keyInput = it },
                        modifier               = Modifier.fillMaxWidth(),
                        placeholder            = { Text("sk-ant-…", fontSize = 11.sp, color = Color(0xFF8899AA)) },
                        singleLine             = true,
                        visualTransformation   = PasswordVisualTransformation(),
                        colors                 = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = Color(0xFF4FC3F7),
                            unfocusedBorderColor    = Color(0xFF2A3A4A),
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            focusedContainerColor   = Color(0xFF0D1520),
                            unfocusedContainerColor = Color(0xFF0D1520),
                        ),
                    )
                    Button(
                        onClick  = { onSaveKey(keyInput); keyInput = "" },
                        enabled  = keyInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    ) {
                        Text("Save Key")
                    }
                }
            }

            // Generate button
            Button(
                onClick  = onGenerate,
                enabled  = !state.briefLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            ) {
                if (state.briefLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Analyzing ${state.articles.size} headlines…")
                } else {
                    Text(
                        if (state.claudeApiKey.isBlank()) "Generate Local Summary" else "Generate AI Intelligence Brief",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Brief output
            if (state.aiBrief.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1520))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("SITUATION REPORT", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold,
                            fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(state.aiBrief, color = Color(0xFFE0E0E0), fontSize = 13.sp, lineHeight = 21.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
