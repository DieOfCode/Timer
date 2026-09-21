package com.example.timer.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.timer.model.TimerSound
import com.example.timer.settings.TimerSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateTimerFlowViewModel(
    private val settingsRepository: TimerSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTimerUiState())
    val uiState: StateFlow<CreateTimerUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "created: ${System.identityHashCode(this)}")

        viewModelScope.launch {
            val settings = settingsRepository.settings.first()

            _uiState.update { current ->
                if (current.isInitialized) {
                    current
                } else {
                    current.copy(
                        durationMinutes = settings.defaultDurationMinutes,
                        sound = settings.defaultSound,
                        isInitialized = true
                    )
                }
            }
        }
    }

    fun setDurationMinutes(minutes: Int) {
        _uiState.update { current ->
            current.copy(durationMinutes = minutes.coerceIn(1, 1_440))
        }
    }

    fun selectSound(sound: TimerSound) {
        _uiState.update { current ->
            current.copy(sound = sound)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "cleared: ${System.identityHashCode(this)}")
        super.onCleared()
    }

    class Factory(
        private val settingsRepository: TimerSettingsRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            if (
                modelClass.isAssignableFrom(
                    CreateTimerFlowViewModel::class.java
                )
            ) {
                @Suppress("UNCHECKED_CAST")
                return CreateTimerFlowViewModel(
                    settingsRepository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }

    private companion object {
        const val TAG = "CreateTimerFlowVM"
    }
}
