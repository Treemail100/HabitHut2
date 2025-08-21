package com.habithut.util

import android.content.Context
import java.time.LocalTime

object FocusPrefs {
    private const val PREF = "focus_prefs"
    private const val KEY_START_H = "start_h"
    private const val KEY_START_M = "start_m"
    private const val KEY_END_H = "end_h"
    private const val KEY_END_M = "end_m"

    fun setStart(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_START_H, hour).putInt(KEY_START_M, minute).apply()
    }

    fun setEnd(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_END_H, hour).putInt(KEY_END_M, minute).apply()
    }

    fun getStart(context: Context): LocalTime? {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!sp.contains(KEY_START_H)) return null
        return LocalTime.of(sp.getInt(KEY_START_H, 9), sp.getInt(KEY_START_M, 0))
    }

    fun getEnd(context: Context): LocalTime? {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!sp.contains(KEY_END_H)) return null
        return LocalTime.of(sp.getInt(KEY_END_H, 17), sp.getInt(KEY_END_M, 0))
    }

    fun isWithinFocusNow(context: Context): Boolean {
        val start = getStart(context) ?: return true // if not set, treat as always on
        val end = getEnd(context) ?: return true
        val now = LocalTime.now()
        return if (end.isAfter(start) || end.equals(start)) {
            now >= start && now <= end
        } else {
            now >= start || now <= end
        }
    }
}