package com.example.sitamu.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthPreferences(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_NAME = stringPreferencesKey("name")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_IS_LOGGED_IN] ?: false
        }

    val adminUsername: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USERNAME]
        }

    val adminName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_NAME]
        }

    suspend fun saveSession(username: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_USERNAME] = username
            preferences[KEY_NAME] = name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences[KEY_USERNAME] = ""
            preferences[KEY_NAME] = ""
        }
    }
}
