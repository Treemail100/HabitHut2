package com.habithut.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    suspend fun countActiveHabits(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteCompletion(habitId: Long, dateKey: String)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateKey DESC")
    fun observeCompletions(habitId: Long): Flow<List<HabitCompletion>>

    @Query(
        "SELECT h.*, COUNT(c.id) as count FROM habits h LEFT JOIN habit_completions c ON h.id = c.habitId WHERE h.isArchived = 0 GROUP BY h.id ORDER BY h.createdAt DESC"
    )
    fun observeHabitsWithCounts(): Flow<List<HabitWithCount>>
}

data class HabitWithCount(
    val id: Long,
    val title: String,
    val description: String,
    val type: HabitType,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val daysOfWeekMask: Int,
    val isArchived: Boolean,
    val createdAt: Long,
    val count: Int
)