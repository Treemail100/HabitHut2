package com.habithut

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class HabitHutApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_REMINDERS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            reminderChannel.description = "Reminders to complete habits and goals"

            val focusChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_FOCUS,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            )
            focusChannel.description = "Persistent notification while app blocking is active"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(focusChannel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_REMINDERS = "habit_reminders"
        const val NOTIFICATION_CHANNEL_FOCUS = "focus_mode"
    }
}