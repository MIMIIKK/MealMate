package com.kanung.mealmate.data.models

import java.util.Date

data class MealPlan(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val meals: Map<String, List<MealItem>> = mapOf(), // Day -> List of meals
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)