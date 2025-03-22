package com.kanung.mealmate.data.models

import java.util.Date

data class Recipe(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTime: Int = 0, // in minutes
    val cookTime: Int = 0, // in minutes
    val servings: Int = 1,
    val imageUrl: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)