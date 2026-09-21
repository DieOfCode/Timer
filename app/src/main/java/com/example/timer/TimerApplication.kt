package com.example.timer
import android.app.Application
import androidx.room.Room
import com.example.timer.data.local.TimerDatabase
import com.example.timer.history.data.TimerHistoryRepository
import com.example.timer.notification.TimerNotificationManager
import com.example.timer.settings.TimerSettingsRepository

class TimerApplication : Application() {

    val timerSettingsRepository by lazy {
        TimerSettingsRepository(this)
    }

    private val timerDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TimerDatabase::class.java,
            TimerDatabase.DATABASE_NAME
        ).build()
    }

    val timerHistoryRepository by lazy {
        TimerHistoryRepository(timerDatabase.timerHistoryDao())
    }

    override fun onCreate() {
        super.onCreate()

        TimerNotificationManager.createChannels(this)
    }
}
