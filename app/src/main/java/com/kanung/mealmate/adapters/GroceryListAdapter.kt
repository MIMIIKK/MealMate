package com.kanung.mealmate.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.databinding.ItemGroceryBinding

class GroceryListAdapter(
    private val onItemClick: (GroceryItem) -> Unit,
    private val onCheckChanged: (GroceryItem, Boolean) -> Unit,
    private val onDeleteClick: (GroceryItem) -> Unit
) : ListAdapter<GroceryItem, GroceryListAdapter.GroceryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val binding = ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroceryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class GroceryViewHolder(private val binding: ItemGroceryBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCheckChanged(getItem(position), isChecked)
                }
            }

            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(item: GroceryItem) {
            binding.textViewItemName.text = item.name
            binding.textViewItemCategory.text = item.category
            binding.textViewItemQuantity.text = item.quantity.toString()
            binding.textViewItemPrice.text = String.format("$%.2f", item.price)

            // Need to remove the listener temporarily to avoid callback loop
            binding.checkboxCompleted.setOnCheckedChangeListener(null)
            binding.checkboxCompleted.isChecked = item.isPurchased
            // Re-set the listener
            binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCheckChanged(getItem(position), isChecked)
                }
            }

            // Apply strikethrough if item is purchased
            if (item.isPurchased) {
                binding.textViewItemName.paintFlags = binding.textViewItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.textViewItemName.paintFlags = binding.textViewItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GroceryItem>() {
            override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}