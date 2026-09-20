package com.example.timer.settings

import com.example.timer.model.TimerSound

data class TimerSettings(
    val defaultDurationMinutes: Int = DEFAULT_DURATION_MINUTES,
    val defaultSound: TimerSound = TimerSound.BELL
)

const val DEFAULT_DURATION_MINUTES = 5
