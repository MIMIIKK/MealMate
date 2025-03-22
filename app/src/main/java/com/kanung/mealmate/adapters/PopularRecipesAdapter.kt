package com.kanung.mealmate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.ItemPopularRecipeBinding
import com.kanung.mealmate.data.models.Recipe

class PopularRecipesAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, PopularRecipesAdapter.PopularRecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularRecipeViewHolder {
        val binding = ItemPopularRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularRecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PopularRecipeViewHolder(private val binding: ItemPopularRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.buttonFavorite.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(getItem(position))
                }
            }
        }

        fun bind(recipe: Recipe) {
            binding.textViewTitle.text = recipe.title
            binding.textViewCategory.text = recipe.category
            binding.textViewTime.text = binding.root.context.getString(
                R.string.total_time_format,
                recipe.prepTime + recipe.cookTime
            )

            // Set favorite icon
            val favoriteIcon = if (recipe.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            binding.buttonFavorite.setImageResource(favoriteIcon)

            // Load image with Glide
            if (recipe.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .centerCrop()
                    .into(binding.imageViewRecipe)
            } else {
                binding.imageViewRecipe.setImageResource(R.drawable.placeholder_recipe)
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}