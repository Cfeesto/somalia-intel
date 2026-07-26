package com.somalia.intel.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "somalia_intel_prefs")

class AppPreferences(private val context: Context) {

    private val KEY_CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
    private val KEY_AI_BRIEF       = stringPreferencesKey("cached_ai_brief")

    val claudeApiKey: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_CLAUDE_API_KEY] ?: "" }

    val cachedBrief: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_AI_BRIEF] ?: "" }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs -> prefs[KEY_CLAUDE_API_KEY] = key }
    }

    suspend fun saveBrief(brief: String) {
        context.dataStore.edit { prefs -> prefs[KEY_AI_BRIEF] = brief }
    }
}
