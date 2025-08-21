package com.habithut.repository

import android.content.Context
import com.habithut.billing.PremiumAccess
import com.habithut.data.AppDatabase
import com.habithut.data.BlockedApp
import com.habithut.data.Habit
import com.habithut.data.HabitCompletion
import com.habithut.data.HabitDao
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    context: Context,
    private val premiumAccess: PremiumAccess
) {
    private val db: AppDatabase = AppDatabase.get(context)
    private val habitDao: HabitDao = db.habitDao()

    fun observeActiveHabits() = habitDao.observeActiveHabits()

    suspend fun upsertHabitEnforcingLimit(habit: Habit): Long {
        val currentCount = habitDao.countActiveHabits()
        val allowed = if (premiumAccess.isPremium()) Int.MAX_VALUE else 3
        if (habit.id == 0L && currentCount >= allowed) {
            throw IllegalStateException("Free tier allows up to 3 active habits/goals.")
        }
        return habitDao.upsertHabit(habit)
    }

    suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

    fun observeCompletions(habitId: Long) = habitDao.observeCompletions(habitId)

    suspend fun toggleCompletion(habitId: Long, dateKey: String) {
        val inserted = habitDao.insertCompletion(
            HabitCompletion(habitId = habitId, dateKey = dateKey)
        )
        if (inserted == -1L) {
            habitDao.deleteCompletion(habitId, dateKey)
        }
    }

    // Blocked apps
    fun observeBlockedApps() = db.blockedAppDao().observeBlocked()

    suspend fun addBlockedAppEnforcingLimit(packageName: String) {
        val current = db.blockedAppDao().count()
        val allowed = if (premiumAccess.isPremium()) Int.MAX_VALUE else 1
        if (current >= allowed) throw IllegalStateException("Free tier allows blocking 1 app.")
        db.blockedAppDao().insert(BlockedApp(packageName = packageName))
    }

    suspend fun getBlockedPackages(): List<String> = db.blockedAppDao().getAll().map { it.packageName }
}