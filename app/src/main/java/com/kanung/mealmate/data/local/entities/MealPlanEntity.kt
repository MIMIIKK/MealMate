package com.kanung.mealmate.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kanung.mealmate.data.models.MealPlan
import java.util.Date

@Entity(
    tableName = "meal_plans",
    indices = [Index("userId")]
)
data class MealPlanEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val createdAt: Date,
    val updatedAt: Date
) {
    companion object {
        fun fromMealPlan(mealPlan: MealPlan): MealPlanEntity {
            return MealPlanEntity(
                id = mealPlan.id,
                userId = mealPlan.userId,
                title = mealPlan.title,
                description = mealPlan.description,
                startDate = mealPlan.startDate,
                endDate = mealPlan.endDate,
                createdAt = mealPlan.createdAt ?: Date(),
                updatedAt = mealPlan.updatedAt ?: Date()
            )
        }
    }

    fun toMealPlan(meals: List<MealItemEntity>): MealPlan {
        // Convert List<MealItemEntity> to Map<String, List<MealItem>>
        val mealsByDay = meals.groupBy { it.day }.mapValues { (_, items) ->
            items.map { it.toMealItem() }
        }

        return MealPlan(
            id = id,
            userId = userId,
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            meals = mealsByDay,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}