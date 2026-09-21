package com.example.timer.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timer.TimerApplication
import com.example.timer.model.TimerSound
import com.example.timer.notification.TimerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val durationMillis = intent.getLongExtra(
            EXTRA_TIMER_DURATION_MILLIS,
            0L
        )

        TimerNotificationManager.showTimerFinished(
            context = context,
            sound = sound
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                (context.applicationContext as TimerApplication)
                    .timerHistoryRepository
                    .recordTimerFinished(
                        durationMillis = durationMillis,
                        finishedAtEpochMillis = System.currentTimeMillis(),
                        sound = sound
                    )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TIMER_FINISHED =
            "com.example.timer.action.TIMER_FINISHED"

        const val EXTRA_TIMER_SOUND = "timer_sound"
        const val EXTRA_TIMER_DURATION_MILLIS = "timer_duration_millis"
    }
}
