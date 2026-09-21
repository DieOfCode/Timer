package com.example.timer.timer

import com.example.timer.model.TimerSound

data class TimerUiState(
    val status: TimerStatus = TimerStatus.READY,
    val durationMillis: Long = DEFAULT_TIMER_DURATION_MILLIS,
    val remainingMillis: Long = durationMillis,
    val sound: TimerSound = TimerSound.BELL,
    val finishTimeElapsedRealtime: Long? = null
)

const val DEFAULT_TIMER_DURATION_MILLIS = 5 * 60 * 1_000L
