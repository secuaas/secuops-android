package com.secuaas.secuops.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secuops_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(email: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_ROLE_KEY] = role
        }
    }

    fun getToken(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[TOKEN_KEY]
            }.first()
        }
    }

    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    fun getUserEmail(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[USER_EMAIL_KEY]
            }.first()
        }
    }

    fun getUserRole(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[USER_ROLE_KEY]
            }.first()
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
