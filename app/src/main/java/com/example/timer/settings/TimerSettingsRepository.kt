package com.example.timer.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timer.model.TimerSound
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.timerSettingsDataStore:
    DataStore<Preferences> by preferencesDataStore(
        name = "timer_settings"
    )

class TimerSettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.timerSettingsDataStore

    val settings: Flow<TimerSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            val durationMinutes = preferences[DEFAULT_DURATION_KEY]
                ?.coerceIn(MIN_DURATION_MINUTES, MAX_DURATION_MINUTES)
                ?: DEFAULT_DURATION_MINUTES

            val sound = preferences[DEFAULT_SOUND_KEY]
                ?.let { savedName ->
                    runCatching {
                        TimerSound.valueOf(savedName)
                    }.getOrNull()
                }
                ?: TimerSound.BELL

            TimerSettings(
                defaultDurationMinutes = durationMinutes,
                defaultSound = sound
            )
        }

    suspend fun save(settings: TimerSettings) {
        val safeDuration = settings.defaultDurationMinutes.coerceIn(
            MIN_DURATION_MINUTES,
            MAX_DURATION_MINUTES
        )

        dataStore.edit { preferences ->
            preferences[DEFAULT_DURATION_KEY] = safeDuration
            preferences[DEFAULT_SOUND_KEY] = settings.defaultSound.name
        }
    }

    private companion object {
        const val MIN_DURATION_MINUTES = 1
        const val MAX_DURATION_MINUTES = 1_440

        val DEFAULT_DURATION_KEY =
            intPreferencesKey("default_duration_minutes")

        val DEFAULT_SOUND_KEY =
            stringPreferencesKey("default_timer_sound")
    }
}
