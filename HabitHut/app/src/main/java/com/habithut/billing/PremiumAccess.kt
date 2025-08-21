package com.habithut.billing

import android.content.Context
import android.content.SharedPreferences

interface PremiumAccess {
    fun isPremium(): Boolean
}

class PremiumAccessImpl(context: Context) : PremiumAccess {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("premium", Context.MODE_PRIVATE)

    override fun isPremium(): Boolean = prefs.getBoolean(KEY_PREMIUM, false)

    fun setPremium(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM, value).apply()
    }

    companion object {
        private const val KEY_PREMIUM = "is_premium"
    }
}