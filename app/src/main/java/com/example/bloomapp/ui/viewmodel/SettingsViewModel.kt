package com.example.bloomapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension DataStore pour le contexte
val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "settings")

// Clés de préférences
object PreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LANGUAGE = stringPreferencesKey("language")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class Language(val code: String, val displayName: String, val flag: String) {
    FRENCH("fr", "Français", "🇫🇷")
}

class SettingsViewModel(private val context: Context) : ViewModel() {

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var selectedLanguage by mutableStateOf(Language.FRENCH)
    var notificationsEnabled by mutableStateOf(true)
    var autoBackupEnabled by mutableStateOf(false)

    private val scope = CoroutineScope(Dispatchers.IO)

    /** Flows pour lire les préférences **/
    val themeModeFlow = context.dataStore.data.map { it[PreferencesKeys.THEME_MODE] ?: "system" }
    val languageFlow = context.dataStore.data.map { it[PreferencesKeys.LANGUAGE] ?: "fr" }
    val notificationsFlow = context.dataStore.data.map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }
    val autoBackupFlow = context.dataStore.data.map { it[PreferencesKeys.AUTO_BACKUP] ?: false }

    /** Fonctions pour sauvegarder les préférences **/
    fun saveThemeMode(mode: ThemeMode) {
        scope.launch { context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name.lowercase() }; themeMode = mode }
    }
    fun saveLanguage(language: Language) {
        scope.launch { context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = language.code }; selectedLanguage = language }
    }
    fun saveNotifications(enabled: Boolean) {
        scope.launch { context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }; notificationsEnabled = enabled }
    }
    fun saveAutoBackup(enabled: Boolean) {
        scope.launch { context.dataStore.edit { it[PreferencesKeys.AUTO_BACKUP] = enabled }; autoBackupEnabled = enabled }
    }
}

