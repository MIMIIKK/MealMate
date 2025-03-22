package com.kanung.mealmate.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kanung.mealmate.data.local.entities.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // Get all recipes as LiveData
    @Query("SELECT * FROM recipes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRecipes(userId: String): LiveData<List<RecipeEntity>>

    // Get all recipes as Flow
    @Query("SELECT * FROM recipes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRecipesFlow(userId: String): Flow<List<RecipeEntity>>

    // Get all recipes synchronously
    @Query("SELECT * FROM recipes WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllRecipesSync(userId: String): List<RecipeEntity>

    // Get a recipe by ID
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    // Insert a single recipe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    // Insert multiple recipes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    // Update an existing recipe
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    // Delete a recipe by ID
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    // Delete a recipe
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    // Update favorite status of a recipe
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean)

    // Search for recipes based on a query
    @Query("SELECT * FROM recipes WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    suspend fun searchRecipes(query: String, userId: String): List<RecipeEntity>
}