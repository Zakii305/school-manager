package com.school.manager.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
    private val REMEMBER = booleanPreferencesKey("remember_me")
    private val SAVED_EMAIL = stringPreferencesKey("saved_email")
    private val SAVED_PASSWORD = stringPreferencesKey("saved_password")
    private val SCHOOL_NAME = stringPreferencesKey("school_name")
    private val APP_THEME = stringPreferencesKey("app_theme")

    val followSystem: Flow<Boolean> = context.dataStore.data.map { it[FOLLOW_SYSTEM] ?: true }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val rememberMe: Flow<Boolean> = context.dataStore.data.map { it[REMEMBER] ?: false }
    val savedEmail: Flow<String> = context.dataStore.data.map { it[SAVED_EMAIL] ?: "" }
    val savedPassword: Flow<String> = context.dataStore.data.map { it[SAVED_PASSWORD] ?: "" }
    val schoolName: Flow<String> = context.dataStore.data.map {
        it[SCHOOL_NAME] ?: "School Manager"
    }
    val appTheme: Flow<String> = context.dataStore.data.map { it[APP_THEME] ?: "classic" }

    suspend fun setFollowSystem(v: Boolean) { context.dataStore.edit { it[FOLLOW_SYSTEM] = v } }
    suspend fun setDarkMode(v: Boolean) { context.dataStore.edit { it[DARK_MODE] = v } }
    suspend fun setSchoolName(v: String) { context.dataStore.edit { it[SCHOOL_NAME] = v } }
    suspend fun setAppTheme(v: String) { context.dataStore.edit { it[APP_THEME] = v } }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit {
            it[REMEMBER] = true
            it[SAVED_EMAIL] = email
            it[SAVED_PASSWORD] = password
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit {
            it[REMEMBER] = false
            it[SAVED_EMAIL] = ""
            it[SAVED_PASSWORD] = ""
        }
    }
}
