package com.example.timer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.timer.model.TimerSound
import java.io.IOException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultDurationMinutes: Int = DEFAULT_DURATION_MINUTES,
    val defaultSound: TimerSound = TimerSound.BELL,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

sealed interface SettingsEvent {
    data object Saved : SettingsEvent
    data object SaveFailed : SettingsEvent
}

class SettingsViewModel(
    private val repository: TimerSettingsRepository
) : ViewModel() {

    private val isSaving = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.settings,
        isSaving
    ) { settings, saving ->
            SettingsUiState(
                defaultDurationMinutes = settings.defaultDurationMinutes,
                defaultSound = settings.defaultSound,
                isLoading = false,
                isSaving = saving
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsUiState()
        )

    private val eventsChannel = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    fun saveSettings(
        durationMinutes: Int,
        sound: TimerSound
    ) {
        if (isSaving.value) return

        isSaving.value = true

        viewModelScope.launch {
            try {
                repository.save(
                    TimerSettings(
                        defaultDurationMinutes = durationMinutes,
                        defaultSound = sound
                    )
                )
                eventsChannel.send(SettingsEvent.Saved)
            } catch (_: IOException) {
                eventsChannel.send(SettingsEvent.SaveFailed)
            } finally {
                isSaving.value = false
            }
        }
    }

    class Factory(
        private val repository: TimerSettingsRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}
