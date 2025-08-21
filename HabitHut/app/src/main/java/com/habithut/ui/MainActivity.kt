package com.habithut.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habithut.billing.BillingManager
import com.habithut.billing.PremiumAccessImpl
import com.habithut.databinding.ActivityMainBinding
import com.habithut.databinding.ItemHabitBinding
import com.habithut.focus.FocusBlockingService

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val premiumAccess = PremiumAccessImpl(this)
        billingManager = BillingManager(this, premiumAccess)
        billingManager.connect()

        val adapter = HabitsAdapter { habitId -> viewModel.toggleCompletion(habitId) }
        binding.recyclerHabits.layoutManager = LinearLayoutManager(this)
        binding.recyclerHabits.adapter = adapter

        viewModel.habits.observe(this) { habits ->
            adapter.submitList(habits)
            binding.emptyState.isVisible = habits.isNullOrEmpty()
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        binding.buttonAdd.setOnClickListener {
            startActivity(Intent(this, AddEditHabitActivity::class.java))
        }

        binding.buttonBlockPackage.setOnClickListener {
            val pkg = binding.inputPackage.text?.toString()?.trim().orEmpty()
            if (pkg.isEmpty()) {
                Toast.makeText(this, "Enter a package name", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addBlockedApp(pkg) { ok, msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonStartFocus.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                startService(Intent(this, FocusBlockingService::class.java))
            }
        }

        binding.buttonGoPremium.setOnClickListener {
            billingManager.launchPurchaseFlow(this, BillingManager.PRODUCT_MONTHLY)
        }
    }
}

private class HabitsAdapter(
    val onToggle: (Long) -> Unit
) : RecyclerView.Adapter<HabitViewHolder>() {
    private var items: List<com.habithut.data.Habit> = emptyList()

    fun submitList(list: List<com.habithut.data.Habit>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding, onToggle)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

private class HabitViewHolder(
    private val binding: ItemHabitBinding,
    private val onToggle: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(habit: com.habithut.data.Habit) {
        binding.textTitle.text = habit.title
        binding.buttonComplete.setOnClickListener { onToggle(habit.id) }
    }
}