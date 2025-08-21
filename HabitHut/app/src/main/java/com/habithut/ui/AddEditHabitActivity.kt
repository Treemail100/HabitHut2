package com.habithut.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.habithut.databinding.ActivityAddEditHabitBinding

class AddEditHabitActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityAddEditHabitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSave.setOnClickListener {
            val title = binding.inputTitle.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show()
            } else {
                val hour = binding.timeReminder.hour
                val minute = binding.timeReminder.minute
                viewModel.addHabitWithReminder(title, hour, minute)
                finish()
            }
        }
    }
}