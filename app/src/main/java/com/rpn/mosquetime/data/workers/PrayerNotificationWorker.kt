package com.rpn.mosquetime.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rpn.mosquetime.utils.NotificationUtil

class PrayerNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Prayer Time"
        val message = inputData.getString("message") ?: "It's prayer time!"
        val notificationId = inputData.getInt("notificationId", 0)

        NotificationUtil.showNotification(applicationContext, title, message, notificationId)
        return Result.success()
    }
}