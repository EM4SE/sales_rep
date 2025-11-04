package com.salesrep.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// This ensures only ONE instance exists for the entire application
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Access the single DataStore instance through the context
    private val dataStore: DataStore<Preferences>
        get() = context.dataStore

    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
        val USER_ID = intPreferencesKey(Constants.KEY_USER_ID)
        val USER_TYPE = stringPreferencesKey(Constants.KEY_USER_TYPE)
        val USER_NAME = stringPreferencesKey(Constants.KEY_USER_NAME)
        val USER_EMAIL = stringPreferencesKey(Constants.KEY_USER_EMAIL)
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    fun getAuthToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTH_TOKEN]
    }

    suspend fun saveUserData(userId: Int, userType: String, name: String, email: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_TYPE] = userType
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    fun getUserId(): Flow<Int?> = dataStore.data.map {
        it[PreferencesKeys.USER_ID]
    }

    fun getUserType(): Flow<String?> = dataStore.data.map {
        it[PreferencesKeys.USER_TYPE]
    }

    fun getUserName(): Flow<String?> = dataStore.data.map {
        it[PreferencesKeys.USER_NAME]
    }

    fun getUserEmail(): Flow<String?> = dataStore.data.map {
        it[PreferencesKeys.USER_EMAIL]
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}