package com.example.timer.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.timer.MainActivity
import com.example.timer.R
import com.example.timer.model.TimerSound

object TimerNotificationManager {

    private const val BELL_CHANNEL_ID = "timer_finished_bell"
    private const val SILENT_CHANNEL_ID = "timer_finished_silent"
    private const val FINISHED_NOTIFICATION_ID = 1001
    private const val OPEN_APP_REQUEST_CODE = 2001

    fun createChannels(context: Context) {
        val bellChannel = NotificationChannel(
            BELL_CHANNEL_ID,
            context.getString(R.string.timer_bell_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(
                R.string.timer_bell_channel_description
            )

            enableVibration(true)
        }

        val silentChannel = NotificationChannel(
            SILENT_CHANNEL_ID,
            context.getString(R.string.timer_silent_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(
                R.string.timer_silent_channel_description
            )

            setSound(null, null)
            enableVibration(false)
        }

        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannels(
            listOf(
                bellChannel,
                silentChannel
            )
        )
    }

    fun showTimerFinished(
        context: Context,
        sound: TimerSound
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager =
            NotificationManagerCompat.from(context)

        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (sound) {
            TimerSound.BELL -> BELL_CHANNEL_ID
            TimerSound.SILENT -> SILENT_CHANNEL_ID
        }

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(
                context.getString(R.string.timer_finished_title)
            )
            .setContentText(
                context.getString(R.string.timer_finished_message)
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            FINISHED_NOTIFICATION_ID,
            notification
        )
    }
}
