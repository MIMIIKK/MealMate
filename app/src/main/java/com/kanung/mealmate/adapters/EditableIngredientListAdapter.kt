package com.kanung.mealmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanung.mealmate.databinding.ItemEditableIngredientBinding
import com.kanung.mealmate.data.models.Ingredient

class EditableIngredientListAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<Ingredient, EditableIngredientListAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemEditableIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IngredientViewHolder(private val binding: ItemEditableIngredientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonDelete.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(currentPosition)
                }
            }
        }

        fun bind(ingredient: Ingredient) {
            val quantityWithUnit = if (ingredient.unit.isNotEmpty()) {
                "${ingredient.quantity} ${ingredient.unit}"
            } else {
                "${ingredient.quantity}"
            }

            binding.textViewIngredientName.text = ingredient.name
            binding.textViewIngredientQuantity.text = quantityWithUnit
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }
}