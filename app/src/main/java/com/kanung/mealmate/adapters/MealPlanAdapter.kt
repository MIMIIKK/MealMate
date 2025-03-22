package com.kanung.mealmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.ItemMealPlanBinding
import com.kanung.mealmate.data.models.MealPlan
import java.text.SimpleDateFormat
import java.util.Locale

class MealPlanAdapter(private val listener: MealPlanListener) :
    ListAdapter<MealPlan, MealPlanAdapter.MealPlanViewHolder>(MealPlanDiffCallback()) {

    interface MealPlanListener {
        fun onMealPlanClick(mealPlan: MealPlan)
        fun onDeleteMealPlan(mealPlan: MealPlan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealPlanViewHolder {
        val binding = ItemMealPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealPlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealPlanViewHolder, position: Int) {
        val mealPlan = getItem(position)
        holder.bind(mealPlan)
    }

    inner class MealPlanViewHolder(private val binding: ItemMealPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMealPlanClick(getItem(position))
                }
            }

            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteMealPlan(getItem(position))
                }
            }
        }

        fun bind(mealPlan: MealPlan) {
            binding.textViewPlanTitle.text = mealPlan.title
            binding.textViewPlanDescription.text = mealPlan.description

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateRange = "${dateFormat.format(mealPlan.startDate)} - ${dateFormat.format(mealPlan.endDate)}"
            binding.textViewDateRange.text = dateRange

            // Count total meals across all days
            val totalMeals = mealPlan.meals.values.sumOf { it.size }
            binding.textViewMealCount.text = binding.root.context.resources.getQuantityString(
                R.plurals.meal_count, totalMeals, totalMeals
            )
        }
    }

    class MealPlanDiffCallback : DiffUtil.ItemCallback<MealPlan>() {
        override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem == newItem
        }
    }
}