package com.example.timer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.timer.history.data.TimerHistoryDao
import com.example.timer.history.data.TimerHistoryEntity

@Database(
    entities = [TimerHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TimerDatabase : RoomDatabase() {

    abstract fun timerHistoryDao(): TimerHistoryDao

    companion object {
        const val DATABASE_NAME = "timer_database"
    }
}
