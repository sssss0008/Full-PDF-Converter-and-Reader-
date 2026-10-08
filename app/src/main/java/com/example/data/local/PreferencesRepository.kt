package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pdfomni_settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("has_completed_onboarding")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        val KEY_OLED_INVERT = booleanPreferencesKey("oled_invert_colors")
        val KEY_VIEW_MODE = stringPreferencesKey("default_view_mode") // CONTINUOUS, SINGLE
        val KEY_VAULT_PIN = stringPreferencesKey("vault_pin_code")
        val KEY_PROFILE_NAME = stringPreferencesKey("profile_name")
        val KEY_PROFILE_EMAIL = stringPreferencesKey("profile_email")
        val KEY_PROFILE_TITLE = stringPreferencesKey("profile_title")
        val KEY_PROFILE_COMPANY = stringPreferencesKey("profile_company")
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true // Obsidian Dark by default
    }

    val isOledInvert: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_OLED_INVERT] ?: false
    }

    val defaultViewMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_VIEW_MODE] ?: "CONTINUOUS"
    }

    val vaultPin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_VAULT_PIN]
    }

    val profileName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_NAME] ?: "Alex Mercer"
    }

    val profileEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_EMAIL] ?: "alex.mercer@enterprise.io"
    }

    val profileTitle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_TITLE] ?: "Senior Systems Architect"
    }

    val profileCompany: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_COMPANY] ?: "Omni Systems Global"
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setOledInvert(enabled: Boolean) {
        context.dataStore.edit { it[KEY_OLED_INVERT] = enabled }
    }

    suspend fun setDefaultViewMode(mode: String) {
        context.dataStore.edit { it[KEY_VIEW_MODE] = mode }
    }

    suspend fun setVaultPin(pin: String) {
        context.dataStore.edit { it[KEY_VAULT_PIN] = pin }
    }

    suspend fun updateProfile(name: String, email: String, title: String, company: String) {
        context.dataStore.edit {
            it[KEY_PROFILE_NAME] = name
            it[KEY_PROFILE_EMAIL] = email
            it[KEY_PROFILE_TITLE] = title
            it[KEY_PROFILE_COMPANY] = company
        }
    }
}
