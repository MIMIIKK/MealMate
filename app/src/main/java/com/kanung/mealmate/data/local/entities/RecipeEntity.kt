package com.kanung.mealmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kanung.mealmate.data.local.converters.DateConverters
import com.kanung.mealmate.data.local.converters.IngredientListConverter
import com.kanung.mealmate.data.local.converters.StringListConverter
import com.kanung.mealmate.data.models.Ingredient
import com.kanung.mealmate.data.models.Recipe
import java.util.Date

@Entity(tableName = "recipes")
@TypeConverters(
    IngredientListConverter::class,
    StringListConverter::class,
    DateConverters::class
)
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val imageUrl: String,
    val category: String,
    val tags: List<String>,
    val isFavorite: Boolean,
    val createdAt: Date,
    val updatedAt: Date
) {
    fun toRecipe(): Recipe = Recipe(
        id = id,
        userId = userId,
        title = title,
        description = description,
        ingredients = ingredients,
        instructions = instructions,
        prepTime = prepTime,
        cookTime = cookTime,
        servings = servings,
        imageUrl = imageUrl,
        category = category,
        tags = tags,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromRecipe(recipe: Recipe): RecipeEntity = RecipeEntity(
            id = recipe.id,
            userId = recipe.userId,
            title = recipe.title,
            description = recipe.description,
            ingredients = recipe.ingredients,
            instructions = recipe.instructions,
            prepTime = recipe.prepTime,
            cookTime = recipe.cookTime,
            servings = recipe.servings,
            imageUrl = recipe.imageUrl,
            category = recipe.category,
            tags = recipe.tags,
            isFavorite = recipe.isFavorite,
            createdAt = recipe.createdAt,
            updatedAt = recipe.updatedAt
        )
    }
}