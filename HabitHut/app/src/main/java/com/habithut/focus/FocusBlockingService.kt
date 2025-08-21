package com.habithut.focus

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.habithut.HabitHutApp
import com.habithut.R
import com.habithut.repository.HabitRepository
import com.habithut.ui.MainActivity
import com.habithut.util.FocusPrefs
import com.habithut.util.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FocusBlockingService : Service() {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private lateinit var repository: HabitRepository
    private var pollJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        repository = HabitRepository(this, object : com.habithut.billing.PremiumAccess { override fun isPremium() = true })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_ID, buildNotification())
        startPolling()
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, HabitHutApp.NOTIFICATION_CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Focus Mode Active")
            .setContentText("Blocking distracting apps")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = serviceScope.launch {
            while (true) {
                try {
                    val within = FocusPrefs.isWithinFocusNow(this@FocusBlockingService)
                    val blocked = if (within) repository.getBlockedPackages().toSet() else emptySet<String>()
                    val current = if (within) UsageStatsHelper.getForegroundAppPackage(this@FocusBlockingService) else null
                    if (current != null && blocked.contains(current)) showOverlay() else hideOverlay()
                } catch (_: Exception) {}
                kotlinx.coroutines.delay(700)
            }
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        overlayView = LayoutInflater.from(this).inflate(R.layout.view_focus_overlay, null)
        windowManager?.addView(overlayView, params)
    }

    private fun hideOverlay() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        pollJob?.cancel()
        hideOverlay()
    }

    companion object { const val FOREGROUND_ID = 1001 }
}