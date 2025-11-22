package com.rpn.mosquetime.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rpn.mosquetime.data.workers.PrayerNotificationWorker
import java.util.concurrent.TimeUnit

object NotificationUtil {

    private const val CHANNEL_ID = "mosque_time_channel"
    private const val CHANNEL_NAME = "Mosque Time Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for prayer times and messages"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with actual app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun schedulePrayerNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        delayMinutes: Long
    ) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putInt("notificationId", notificationId)
            .build()

        val prayerWorkRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(prayerWorkRequest)
    }
}