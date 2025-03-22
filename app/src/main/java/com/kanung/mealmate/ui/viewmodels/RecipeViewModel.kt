package com.kanung.mealmate.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.data.repositories.RecipeRepository
import kotlinx.coroutines.launch
import java.io.File

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "RecipeViewModel"
    private val repository = RecipeRepository(application)

    // For live updates from local database
    val recipesLiveData: LiveData<List<Recipe>> = repository.getAllRecipesLiveData()

    // For immediate loading and search results
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _currentRecipe = MutableLiveData<Recipe>()
    val currentRecipe: LiveData<Recipe> = _currentRecipe

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _operationSuccessful = MutableLiveData<Boolean>()
    val operationSuccessful: LiveData<Boolean> = _operationSuccessful

    fun loadAllRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val recipesList = repository.getAllRecipes()
                _recipes.value = recipesList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipes: ${e.message}")
                _error.value = e.message ?: "Failed to load recipes"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipeById(recipeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val recipe = repository.getRecipeById(recipeId)
                _currentRecipe.value = recipe
            } catch (e: Exception) {
                Log.e(TAG, "Error getting recipe by ID: ${e.message}")
                _error.value = e.message ?: "Failed to load recipe details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addRecipe(recipe: Recipe) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.addRecipe(recipe)
                loadAllRecipes() // Refresh the recipe list
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipe: ${e.message}")
                _error.value = e.message ?: "Failed to add recipe"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.updateRecipe(recipe)
                loadAllRecipes() // Refresh the recipe list
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating recipe: ${e.message}")
                _error.value = e.message ?: "Failed to update recipe"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipeId)
                loadAllRecipes() // Refresh the recipe list
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting recipe: ${e.message}")
                _error.value = e.message ?: "Failed to delete recipe"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadRecipeImage(file: File, onSuccess: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val imageUrl = repository.saveRecipeImage(file)
                onSuccess(imageUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image: ${e.message}")
                _error.value = e.message ?: "Failed to upload image"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(recipeId, isFavorite)

                // Update the current recipe if it's the one being modified
                _currentRecipe.value?.let { recipe ->
                    if (recipe.id == recipeId) {
                        _currentRecipe.value = recipe.copy(isFavorite = isFavorite)
                    }
                }

                // Update the recipe list
                _recipes.value?.let { recipeList ->
                    _recipes.value = recipeList.map {
                        if (it.id == recipeId) it.copy(isFavorite = isFavorite) else it
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite: ${e.message}")
                _error.value = e.message ?: "Failed to update favorite status"
            }
        }
    }

    fun searchRecipes(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val recipeList = repository.searchRecipes(query)
                _recipes.value = recipeList
            } catch (e: Exception) {
                Log.e(TAG, "Error searching recipes: ${e.message}")
                _error.value = e.message ?: "Failed to search recipes"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun exportRecipes(onSuccess: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = repository.exportRecipes()
                onSuccess(file)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to export recipes"
            }
        }
    }

    fun importRecipes(file: File) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val count = repository.importRecipes(file)
                _operationSuccessful.value = true
                loadAllRecipes()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to import recipes"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}