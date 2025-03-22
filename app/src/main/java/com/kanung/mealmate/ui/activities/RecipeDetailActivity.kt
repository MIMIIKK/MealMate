package com.kanung.mealmate.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.adapters.IngredientListAdapter
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.databinding.ActivityRecipeDetailBinding
import com.kanung.mealmate.ui.viewmodels.GroceryViewModel
import com.kanung.mealmate.ui.viewmodels.RecipeViewModel

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var viewModel: RecipeViewModel
    private lateinit var ingredientAdapter: IngredientListAdapter
    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId == null) {
            showError("Recipe not found")
            finish()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Load recipe details
        viewModel.getRecipeById(recipeId!!)
    }

    private fun setupRecyclerView() {
        ingredientAdapter = IngredientListAdapter()
        binding.recyclerViewIngredients.adapter = ingredientAdapter
    }

    private fun setupObservers() {
        viewModel.currentRecipe.observe(this) { recipe ->
            // Update UI with recipe details
            binding.textViewRecipeTitle.text = recipe.title
            binding.textViewDescription.text = recipe.description
            binding.textViewPrepTime.text = getString(R.string.prep_time_format, recipe.prepTime)
            binding.textViewCookTime.text = getString(R.string.cook_time_format, recipe.cookTime)
            binding.textViewServings.text = getString(R.string.servings_format, recipe.servings)

            // Update ingredients
            ingredientAdapter.submitList(recipe.ingredients)

            // Update instructions
            val instructionsText = recipe.instructions.mapIndexed { index, instruction ->
                "${index + 1}. $instruction"
            }.joinToString("\n\n")
            binding.textViewInstructions.text = instructionsText

            // Load image
            if (recipe.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .into(binding.imageViewRecipe)
            } else {
                binding.imageViewRecipe.setImageResource(R.drawable.placeholder_recipe)
            }

            // Update favorite button
            updateFavoriteButton(recipe.isFavorite)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                showError(error)
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        // Favorite button
        binding.buttonFavorite.setOnClickListener {
            viewModel.currentRecipe.value?.let { recipe ->
                viewModel.toggleFavorite(recipe.id, !recipe.isFavorite)
            }
        }

        // Edit recipe button
        binding.buttonEdit.setOnClickListener {
            recipeId?.let { id ->
                val intent = android.content.Intent(this, AddRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", id)
                intent.putExtra("IS_EDIT", true)
                startActivity(intent)
            }
        }

        // Delete recipe button
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Add to grocery list button
        binding.buttonAddToGrocery.setOnClickListener {
            viewModel.currentRecipe.value?.let { recipe ->
                // Convert recipe ingredients to grocery items
                val groceryItems = recipe.ingredients.map { ingredient ->
                    val quantity = when (ingredient.quantity) {
                        is Int -> ingredient.quantity // If it's already an Int, use it directly
                        is String -> ingredient.quantity.toIntOrNull() ?: 1 // If it's a String, try to convert it
                        else -> 1 // If it's neither, default to 1
                    }
                    GroceryItem(
                        name = ingredient.name,
                        quantity = quantity,
                        price = 0.0, // Default price, user can update later
                        category = determineCategory(ingredient.name),
                        isPurchased = false
                    )
                }

                // Add to grocery list
                addIngredientsToGrocery(groceryItems)

                Toast.makeText(this, R.string.add_to_grocery_list_success, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun determineCategory(ingredientName: String): String {
        // Simple category determination based on common ingredients
        // This could be enhanced with a more comprehensive ingredient categorization system
        return when {
            listOf("milk", "cheese", "yogurt", "butter", "cream").any { ingredientName.contains(it, ignoreCase = true) } -> "Dairy"
            listOf("chicken", "beef", "pork", "fish", "meat").any { ingredientName.contains(it, ignoreCase = true) } -> "Meat"
            listOf("apple", "banana", "tomato", "lettuce", "onion", "carrot", "potato", "vegetable", "fruit").any {
                ingredientName.contains(it, ignoreCase = true)
            } -> "Produce"
            listOf("bread", "cake", "pastry", "dough").any { ingredientName.contains(it, ignoreCase = true) } -> "Bakery"
            listOf("rice", "pasta", "noodle", "cereal", "flour", "sugar", "salt", "spice").any {
                ingredientName.contains(it, ignoreCase = true)
            } -> "Pantry"
            else -> "Other"
        }
    }

    private fun addIngredientsToGrocery(groceryItems: List<GroceryItem>) {
        // Create an instance of GroceryViewModel to add items
        val groceryViewModel = ViewModelProvider(this)[GroceryViewModel::class.java]

        // Add each ingredient as a grocery item
        groceryItems.forEach { item ->
            groceryViewModel.addGroceryItem(item)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        binding.buttonFavorite.setImageResource(icon)
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_recipe)
            .setMessage(R.string.delete_recipe_confirmation)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                recipeId?.let { id ->
                    viewModel.deleteRecipe(id)
                    finish() // Return to the recipes list
                }
            }
            .show()
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_title_error)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_button_understood) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}