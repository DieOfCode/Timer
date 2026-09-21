package com.example.timer.history.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_history")
data class TimerHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val durationMillis: Long,
    val finishedAtEpochMillis: Long,
    val soundName: String
)
