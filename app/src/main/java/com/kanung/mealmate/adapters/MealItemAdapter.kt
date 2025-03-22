package com.kanung.mealmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanung.mealmate.databinding.ItemMealBinding
import com.kanung.mealmate.data.models.MealItem

class MealItemAdapter(private val listener: MealItemListener) :
    ListAdapter<MealItem, MealItemAdapter.MealItemViewHolder>(MealItemDiffCallback()) {

    interface MealItemListener {
        fun onViewRecipeClick(recipeId: String)
        fun onRemoveMealClick(mealItem: MealItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        val mealItem = getItem(position)
        holder.bind(mealItem)
    }

    inner class MealItemViewHolder(private val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonViewRecipe.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onViewRecipeClick(getItem(position).recipeId)
                }
            }

            binding.buttonRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRemoveMealClick(getItem(position))
                }
            }
        }

        fun bind(mealItem: MealItem) {
            binding.textViewMealType.text = mealItem.mealType
            binding.textViewRecipeName.text = mealItem.recipeName
            binding.textViewServings.text = "Servings: ${mealItem.servings}"

            if (mealItem.notes.isNotEmpty()) {
                binding.textViewNotes.visibility = ViewGroup.VISIBLE
                binding.textViewNotes.text = mealItem.notes
            } else {
                binding.textViewNotes.visibility = ViewGroup.GONE
            }
        }
    }

    class MealItemDiffCallback : DiffUtil.ItemCallback<MealItem>() {
        override fun areItemsTheSame(oldItem: MealItem, newItem: MealItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealItem, newItem: MealItem): Boolean {
            return oldItem == newItem
        }
    }
}