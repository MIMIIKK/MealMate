package com.kanung.mealmate.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.adapters.EditableIngredientListAdapter
import com.kanung.mealmate.databinding.ActivityAddRecipeBinding
import com.kanung.mealmate.data.models.Ingredient
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.ui.viewmodels.RecipeViewModel
import java.io.File
import java.util.Date
import java.util.UUID

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var viewModel: RecipeViewModel
    private lateinit var ingredientAdapter: EditableIngredientListAdapter

    private var isEditMode = false
    private var recipeId: String? = null
    private var selectedImageUri: Uri? = null
    private var imageUrl: String = ""
    private val ingredients = mutableListOf<Ingredient>()  // Using mutable list
    private val tags = mutableListOf<String>()  // Using mutable list

    // Activity result launcher for image picking
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageViewRecipe.setImageURI(it)
            binding.imageViewRecipe.visibility = View.VISIBLE
            binding.buttonRemoveImage.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // Check if we're editing an existing recipe
        recipeId = intent.getStringExtra("RECIPE_ID")
        isEditMode = intent.getBooleanExtra("IS_EDIT", false)

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupBackButton()

        if (isEditMode && recipeId != null) {
            supportActionBar?.title = getString(R.string.edit_recipe)
            viewModel.getRecipeById(recipeId!!)
        } else {
            supportActionBar?.title = getString(R.string.add_recipe)
        }
    }

    private fun setupUI() {
        // Setup category spinner
        val categories = resources.getStringArray(R.array.recipe_categories)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        (binding.spinnerCategory as? AutoCompleteTextView)?.setAdapter(categoryAdapter)
    }

    private fun setupRecyclerView() {
        ingredientAdapter = EditableIngredientListAdapter(
            onDeleteClick = { position ->
                ingredients.removeAt(position)
                ingredientAdapter.notifyItemRemoved(position)
            }
        )
        binding.recyclerViewIngredients.adapter = ingredientAdapter
    }

    private fun setupObservers() {
        viewModel.currentRecipe.observe(this) { recipe ->
            if (isEditMode) {
                // Populate fields with existing recipe data
                binding.editTextTitle.setText(recipe.title)
                binding.editTextDescription.setText(recipe.description)
                binding.editTextPrepTime.setText(recipe.prepTime.toString())
                binding.editTextCookTime.setText(recipe.cookTime.toString())
                binding.editTextServings.setText(recipe.servings.toString())

                // Set category
                val categoryPosition = resources.getStringArray(R.array.recipe_categories).indexOf(recipe.category)
                if (categoryPosition >= 0) {
                    (binding.spinnerCategory as? AutoCompleteTextView)?.setText(recipe.category, false)
                }

                // Set ingredients
                ingredients.clear()
                ingredients.addAll(recipe.ingredients)
                ingredientAdapter.submitList(ingredients.toList())

                // Set instructions
                binding.editTextInstructions.setText(recipe.instructions.joinToString("\n\n"))

                // Set tags
                tags.clear()
                tags.addAll(recipe.tags)
                updateTagsUI()

                // Set image
                imageUrl = recipe.imageUrl
                if (imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_recipe)
                        .error(R.drawable.placeholder_recipe)
                        .into(binding.imageViewRecipe)
                    binding.imageViewRecipe.visibility = View.VISIBLE
                    binding.buttonRemoveImage.visibility = View.VISIBLE
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                showErrorDialog(error)
                viewModel.clearError()
            }
        }

        viewModel.operationSuccessful.observe(this) { success ->
            if (success) {
                Toast.makeText(
                    this,
                    if (isEditMode) R.string.recipe_updated_success else R.string.recipe_added_success,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupListeners() {
        // Add image button
        binding.buttonAddImage.setOnClickListener {
            getContent.launch("image/*")
        }

        // Remove image button
        binding.buttonRemoveImage.setOnClickListener {
            binding.imageViewRecipe.setImageDrawable(null)
            binding.imageViewRecipe.visibility = View.GONE
            binding.buttonRemoveImage.visibility = View.GONE
            selectedImageUri = null
            imageUrl = ""
        }

        // Add ingredient button
        binding.buttonAddIngredient.setOnClickListener {
            val name = binding.editTextIngredientName.text.toString().trim()
            val quantityText = binding.editTextIngredientQuantity.text.toString().trim()
            val unit = binding.editTextIngredientUnit.text.toString().trim()

            if (name.isEmpty()) {
                binding.editTextIngredientName.error = getString(R.string.field_required)
                return@setOnClickListener
            }

            if (quantityText.isEmpty()) {
                binding.editTextIngredientQuantity.error = getString(R.string.field_required)
                return@setOnClickListener
            }

            val quantity = quantityText.toDoubleOrNull()
            if (quantity == null) {
                binding.editTextIngredientQuantity.error = getString(R.string.invalid_number)
                return@setOnClickListener
            }

            val ingredient = Ingredient(
                id = UUID.randomUUID().toString(),
                name = name,
                quantity = quantity,
                unit = unit,
                category = ""  // Optional category
            )

            ingredients.add(ingredient)
            ingredientAdapter.submitList(ingredients.toList())

            // Clear fields
            binding.editTextIngredientName.text?.clear()
            binding.editTextIngredientQuantity.text?.clear()
            binding.editTextIngredientUnit.text?.clear()
        }

        // Add tag button
        binding.buttonAddTag.setOnClickListener {
            val tag = binding.editTextTag.text.toString().trim()

            if (tag.isEmpty()) {
                binding.editTextTag.error = getString(R.string.field_required)
                return@setOnClickListener
            }

            if (!tags.contains(tag)) {
                tags.add(tag)
                updateTagsUI()
                binding.editTextTag.text?.clear()
            }
        }

        // Save recipe button
        binding.buttonSave.setOnClickListener {
            saveRecipe()
        }
    }

    private fun updateTagsUI() {
        binding.chipGroupTags.removeAllViews()

        for (tag in tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                tags.remove(tag)
                binding.chipGroupTags.removeView(chip)
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private fun
            saveRecipe() {
        // Validate inputs
        val title = binding.editTextTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.editTextTitle.error = getString(R.string.field_required)
            return
        }

        val description = binding.editTextDescription.text.toString().trim()
        val prepTimeText = binding.editTextPrepTime.text.toString().trim()
        val cookTimeText = binding.editTextCookTime.text.toString().trim()
        val servingsText = binding.editTextServings.text.toString().trim()

        if (prepTimeText.isEmpty()) {
            binding.editTextPrepTime.error = getString(R.string.field_required)
            return
        }

        if (cookTimeText.isEmpty()) {
            binding.editTextCookTime.error = getString(R.string.field_required)
            return
        }

        if (servingsText.isEmpty()) {
            binding.editTextServings.error = getString(R.string.field_required)
            return
        }

        val prepTime = prepTimeText.toIntOrNull() ?: 0
        val cookTime = cookTimeText.toIntOrNull() ?: 0
        val servings = servingsText.toIntOrNull() ?: 1

        // Get category from AutoCompleteTextView
        val category = (binding.spinnerCategory as? AutoCompleteTextView)?.text.toString()

        // Parse instructions
        val instructionsText = binding.editTextInstructions.text.toString().trim()
        val instructionsList = if (instructionsText.isNotEmpty()) {
            instructionsText.split("\n\n").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        // Check if there are ingredients
        if (ingredients.isEmpty()) {
            showErrorDialog(getString(R.string.no_ingredients_error))
            return
        }

        // Check if there are instructions
        if (instructionsList.isEmpty()) {
            binding.editTextInstructions.error = getString(R.string.field_required)
            return
        }

        // If we have a new image, upload it first
        if (selectedImageUri != null) {
            uploadImage()
        } else {
            // Otherwise, create/update the recipe with the existing or empty image URL
            createOrUpdateRecipe(
                title, description, prepTime, cookTime, servings,
                category, instructionsList
            )
        }
    }

    private fun uploadImage() {
        selectedImageUri?.let { uri ->
            // Create a temporary file from the URI
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("image", ".jpg", cacheDir)

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            viewModel.uploadRecipeImage(tempFile) { uploadedImageUrl ->
                imageUrl = uploadedImageUrl

                // Now create/update the recipe with the new image URL
                val title = binding.editTextTitle.text.toString().trim()
                val description = binding.editTextDescription.text.toString().trim()
                val prepTime = binding.editTextPrepTime.text.toString().toIntOrNull() ?: 0
                val cookTime = binding.editTextCookTime.text.toString().toIntOrNull() ?: 0
                val servings = binding.editTextServings.text.toString().toIntOrNull() ?: 1
                val category = (binding.spinnerCategory as? AutoCompleteTextView)?.text.toString()

                val instructionsText = binding.editTextInstructions.text.toString().trim()
                val instructionsList = if (instructionsText.isNotEmpty()) {
                    instructionsText.split("\n\n").filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

                createOrUpdateRecipe(
                    title, description, prepTime, cookTime, servings,
                    category, instructionsList
                )
            }
        }
    }

    private fun createOrUpdateRecipe(
        title: String, description: String, prepTime: Int, cookTime: Int,
        servings: Int, category: String, instructionsList: List<String>
    ) {
        val currentDate = Date()

        val recipe = if (isEditMode && recipeId != null) {
            // Update existing recipe
            viewModel.currentRecipe.value?.copy(
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructionsList,
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                imageUrl = imageUrl,
                category = category,
                tags = tags,
                updatedAt = currentDate
            ) ?: Recipe(
                id = recipeId!!,
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructionsList,
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                imageUrl = imageUrl,
                category = category,
                tags = tags,
                createdAt = currentDate,
                updatedAt = currentDate
            )
        } else {
            // Create new recipe
            Recipe(
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructionsList,
                prepTime = prepTime,
                cookTime = cookTime,
                servings = servings,
                imageUrl = imageUrl,
                category = category,
                tags = tags,
                createdAt = currentDate,
                updatedAt = currentDate
            )
        }

        if (isEditMode && recipeId != null) {
            viewModel.updateRecipe(recipe)
        } else {
            viewModel.addRecipe(recipe)
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_title_error)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_button_understood) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun setupBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
           finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}