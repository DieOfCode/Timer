package com.example.timer.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.timer.model.TimerSound

object TimerAlarmScheduler {

    enum class ScheduleResult {
        EXACT,
        INEXACT
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return alarmManager(context).canScheduleExactAlarms()
    }

    fun schedule(
        context: Context,
        triggerAtElapsedRealtime: Long,
        sound: TimerSound,
        durationMillis: Long
    ): ScheduleResult {
        val alarmManager = alarmManager(context)
        val pendingIntent = createPendingIntent(
            context = context,
            sound = sound,
            durationMillis = durationMillis
        )

        if (canScheduleExact(context)) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtElapsedRealtime,
                    pendingIntent
                )
                return ScheduleResult.EXACT
            } catch (_: SecurityException) {
                // Доступ мог быть отозван между проверкой и планированием.
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtElapsedRealtime,
            pendingIntent
        )
        return ScheduleResult.INEXACT
    }

    fun cancel(context: Context) {
        val pendingIntent = createPendingIntent(context)

        alarmManager(context).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    private fun createPendingIntent(
        context: Context,
        sound: TimerSound = TimerSound.BELL,
        durationMillis: Long = 0L
    ): PendingIntent {
        val intent = Intent(
            context,
            TimerAlarmReceiver::class.java
        ).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_FINISHED

            putExtra(
                TimerAlarmReceiver.EXTRA_TIMER_SOUND,
                sound.name
            )
            putExtra(
                TimerAlarmReceiver.EXTRA_TIMER_DURATION_MILLIS,
                durationMillis
            )
        }

        return PendingIntent.getBroadcast(
            context,
            FINISHED_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    private const val FINISHED_ALARM_REQUEST_CODE = 3001
}
