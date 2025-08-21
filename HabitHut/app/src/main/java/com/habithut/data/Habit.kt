package com.habithut.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: HabitType = HabitType.HABIT,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val daysOfWeekMask: Int = 0, // bitmask for DayOfWeek.ordinal
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HabitType { HABIT, GOAL }

object DaysOfWeekUtil {
    fun toMask(days: Set<DayOfWeek>): Int = days.fold(0) { acc, day ->
        acc or (1 shl day.ordinal)
    }
    fun fromMask(mask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter { day ->
        mask and (1 shl day.ordinal) != 0
    }.toSet()
}