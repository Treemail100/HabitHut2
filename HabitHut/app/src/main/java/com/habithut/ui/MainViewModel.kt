package com.habithut.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habithut.billing.PremiumAccessImpl
import com.habithut.data.Habit
import com.habithut.reminders.HabitReminderWorker
import com.habithut.repository.HabitRepository
import com.habithut.util.DateUtils
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val premium = PremiumAccessImpl(app)
    private val repository = HabitRepository(app, premium)

    val habits = repository.observeActiveHabits().asLiveData()

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun addHabit(title: String) {
        viewModelScope.launch {
            try {
                repository.upsertHabitEnforcingLimit(Habit(title = title))
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addHabitWithReminder(title: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                repository.upsertHabitEnforcingLimit(Habit(title = title, reminderHour = hour, reminderMinute = minute))
                HabitReminderWorker.scheduleHourly(getApplication())
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addBlockedApp(packageName: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addBlockedAppEnforcingLimit(packageName)
                callback(true, "Blocked $packageName")
            } catch (e: Exception) {
                callback(false, e.message ?: "Error")
            }
        }
    }

    fun clearError() { _error.value = null }

    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, DateUtils.todayKey())
        }
    }
}