package com.kanung.mealmate.data.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.kanung.mealmate.data.local.AppDatabase
import com.kanung.mealmate.data.local.entities.RecipeEntity
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.data.utils.ImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.UUID

class RecipeRepository(private val context: Context) {

    private val TAG = "RecipeRepository"
    private val auth = FirebaseAuth.getInstance()
    private val database = AppDatabase.getDatabase(context)
    private val recipeDao = database.recipeDao()
    private val imageManager = ImageManager(context)

    // Get all recipes as LiveData
    fun getAllRecipesLiveData(): LiveData<List<Recipe>> {
        val userId = auth.currentUser?.uid ?: return MutableLiveData<List<Recipe>>().apply { value = emptyList() }
        return recipeDao.getAllRecipes(userId).map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    // Get recipes for immediate use
    suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext emptyList()
            val recipeEntities = recipeDao.getAllRecipesSync(userId)
            return@withContext recipeEntities.map { it.toRecipe() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all recipes: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun getRecipeById(recipeId: String): Recipe = withContext(Dispatchers.IO) {
        try {
            val recipe = recipeDao.getRecipeById(recipeId)
                ?: throw Exception("Recipe not found")
            return@withContext recipe.toRecipe()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe by ID: ${e.message}")
            throw e
        }
    }

    suspend fun addRecipe(recipe: Recipe): String = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val newRecipeId = UUID.randomUUID().toString()
            val newRecipe = recipe.copy(
                id = newRecipeId,
                userId = userId,
                createdAt = Date(),
                updatedAt = Date()
            )

            val recipeEntity = RecipeEntity.fromRecipe(newRecipe)
            recipeDao.insertRecipe(recipeEntity)

            return@withContext newRecipeId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding recipe: ${e.message}")
            throw e
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Boolean = withContext(Dispatchers.IO) {
        try {
            val updatedRecipe = recipe.copy(updatedAt = Date())
            val recipeEntity = RecipeEntity.fromRecipe(updatedRecipe)
            recipeDao.updateRecipe(recipeEntity)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating recipe: ${e.message}")
            throw e
        }
    }

    suspend fun deleteRecipe(recipeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First get the recipe to delete its image
            val recipe = recipeDao.getRecipeById(recipeId)
            recipe?.let {
                if (it.imageUrl.isNotEmpty() && it.imageUrl.startsWith("file://")) {
                    val path = it.imageUrl.removePrefix("file://")
                    imageManager.deleteImage(path)
                }
            }

            recipeDao.deleteRecipeById(recipeId)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recipe: ${e.message}")
            throw e
        }
    }

    suspend fun saveRecipeImage(file: File): String = withContext(Dispatchers.IO) {
        try {
            val imagePath = imageManager.saveImage(file)
            return@withContext "file://$imagePath"
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image: ${e.message}")
            return@withContext ""
        }
    }

    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            recipeDao.updateFavorite(recipeId, isFavorite)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling favorite: ${e.message}")
            throw e
        }
    }

    suspend fun searchRecipes(query: String): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext emptyList()
            val localResults = recipeDao.searchRecipes(query, userId)
            return@withContext localResults.map { it.toRecipe() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching recipes: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Export/Import functionality
    suspend fun exportRecipes(): File = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val recipes = recipeDao.getAllRecipesSync(userId)

        val exportFile = File(context.cacheDir, "recipe_export.json")
        exportFile.outputStream().use { output ->
            val json = com.google.gson.Gson().toJson(recipes)
            output.write(json.toByteArray())
        }

        return@withContext exportFile
    }

    suspend fun importRecipes(file: File): Int = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        val json = file.readText()
        val type = object : com.google.gson.reflect.TypeToken<List<RecipeEntity>>() {}.type
        val recipes: List<RecipeEntity> = com.google.gson.Gson().fromJson(json, type)

        // Set the userId to the current user
        val userRecipes = recipes.map { it.copy(userId = userId) }

        recipeDao.insertRecipes(userRecipes)
        return@withContext recipes.size
    }
}