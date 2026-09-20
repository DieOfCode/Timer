package com.example.timer.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timer.model.TimerSound
import com.example.timer.notification.TimerNotificationManager

class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != ACTION_TIMER_FINISHED) {
            return
        }

        val sound = intent
            .getStringExtra(EXTRA_TIMER_SOUND)
            ?.let { soundName ->
                runCatching {
                    TimerSound.valueOf(soundName)
                }.getOrNull()
            }
            ?: TimerSound.BELL

        TimerNotificationManager.showTimerFinished(
            context = context,
            sound = sound
        )
    }

    companion object {
        const val ACTION_TIMER_FINISHED =
            "com.example.timer.action.TIMER_FINISHED"

        const val EXTRA_TIMER_SOUND = "timer_sound"
    }
}
