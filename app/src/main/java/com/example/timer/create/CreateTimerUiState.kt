package com.example.timer.create

import com.example.timer.model.TimerSound

data class CreateTimerUiState(
    val durationMinutes: Int = 5,
    val sound: TimerSound = TimerSound.BELL,
    val isInitialized: Boolean = false
)
