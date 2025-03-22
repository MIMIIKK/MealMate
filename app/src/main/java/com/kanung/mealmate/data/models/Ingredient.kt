package com.kanung.mealmate.data.models

data class Ingredient(
    val id: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val category: String = ""
)