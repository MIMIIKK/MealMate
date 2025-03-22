package com.kanung.mealmate.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kanung.mealmate.data.models.MealItem

@Entity(
    tableName = "meal_items",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealPlanId")]
)
data class MealItemEntity(
    @PrimaryKey
    val id: String,
    val mealPlanId: String,
    val recipeId: String,
    val recipeName: String,
    val day: String,
    val mealType: String,
    val servings: Int,
    val notes: String,
    val position: Int = 0
) {
    companion object {
        fun fromMealItem(mealItem: MealItem, mealPlanId: String): MealItemEntity {
            return MealItemEntity(
                id = mealItem.id,
                mealPlanId = mealPlanId,
                recipeId = mealItem.recipeId,
                recipeName = mealItem.recipeName,
                day = mealItem.day,
                mealType = mealItem.mealType,
                servings = mealItem.servings,
                notes = mealItem.notes,
                position = mealItem.position
            )
        }
    }

    fun toMealItem(): MealItem {
        return MealItem(
            id = id,
            recipeId = recipeId,
            recipeName = recipeName,
            day = day,
            mealType = mealType,
            servings = servings,
            notes = notes,
            position = position
        )
    }
}