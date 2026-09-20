package com.example.timer
import android.app.Application
import com.example.timer.notification.TimerNotificationManager
import com.example.timer.settings.TimerSettingsRepository

class TimerApplication : Application() {

    val timerSettingsRepository by lazy {
        TimerSettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()

        TimerNotificationManager.createChannels(this)
    }
}
