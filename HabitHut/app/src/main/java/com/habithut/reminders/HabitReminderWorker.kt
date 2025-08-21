package com.habithut.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habithut.HabitHutApp
import com.habithut.R
import com.habithut.data.AppDatabase
import com.habithut.data.DaysOfWeekUtil
import com.habithut.data.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.util.concurrent.TimeUnit

class HabitReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(applicationContext).habitDao()
        val habits = dao.observeActiveHabits() // not ideal to collect here, so just query directly via Room raw if needed
        // For simplicity in this worker, fetch via Room database openHelper.
        val db = AppDatabase.get(applicationContext).openHelper.readableDatabase
        val cursor = db.query("SELECT id,title,reminderHour,reminderMinute,daysOfWeekMask FROM habits WHERE isArchived=0")
        val now = LocalDateTime.now()
        val today = now.toLocalDate().dayOfWeek

        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val title = cursor.getString(1)
            val hour = cursor.getInt(2)
            val minute = cursor.getInt(3)
            val mask = cursor.getInt(4)
            val days = DaysOfWeekUtil.fromMask(mask)
            if (days.isEmpty() || days.contains(today)) {
                if (now.hour == hour && now.minute == minute) {
                    maybeNotify(id.toInt(), title)
                }
            }
        }
        cursor.close()
        Result.success()
    }

    private fun maybeNotify(id: Int, title: String) {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(applicationContext, HabitHutApp.NOTIFICATION_CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Habit Reminder")
                .setContentText(title)
                .setAutoCancel(true)
                .build()
            manager.notify(id, notification)
        }
    }

    companion object {
        fun scheduleHourly(context: Context) {
            val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(60 - LocalDateTime.now().minute.toLong(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("habit_reminder_hourly", ExistingWorkPolicy.REPLACE, request)
        }
    }
}