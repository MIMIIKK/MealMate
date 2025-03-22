package com.kanung.mealmate.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kanung.mealmate.data.local.entities.MealItemEntity
import com.kanung.mealmate.data.local.entities.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    // Get all meal plans for a user
    @Query("SELECT * FROM meal_plans WHERE userId = :userId ORDER BY startDate DESC")
    fun getAllMealPlans(userId: String): LiveData<List<MealPlanEntity>>

    // Get all meal plans for a user as Flow
    @Query("SELECT * FROM meal_plans WHERE userId = :userId ORDER BY startDate DESC")
    fun getAllMealPlansFlow(userId: String): Flow<List<MealPlanEntity>>

    // Get a meal plan by ID
    @Query("SELECT * FROM meal_plans WHERE id = :planId")
    suspend fun getMealPlanById(planId: String): MealPlanEntity?

    // Get all meal items for a meal plan
    @Query("SELECT * FROM meal_items WHERE mealPlanId = :planId ORDER BY day, position")
    suspend fun getMealItemsForPlan(planId: String): List<MealItemEntity>

    // Get meal items for a specific day of a meal plan
    @Query("SELECT * FROM meal_items WHERE mealPlanId = :planId AND day = :day ORDER BY position")
    suspend fun getMealItemsForDay(planId: String, day: String): List<MealItemEntity>

    // Get meal items for a meal plan as LiveData
    @Query("SELECT * FROM meal_items WHERE mealPlanId = :planId ORDER BY day, position")
    fun getMealItemsForPlanLive(planId: String): LiveData<List<MealItemEntity>>

    // Insert a meal plan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlanEntity): Long

    // Insert meal items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItems(mealItems: List<MealItemEntity>)

    // Update a meal plan
    @Update
    suspend fun updateMealPlan(mealPlan: MealPlanEntity)

    // Delete a meal plan (meal items will be deleted by cascade)
    @Query("DELETE FROM meal_plans WHERE id = :planId")
    suspend fun deleteMealPlan(planId: String)

    // Delete a meal item
    @Query("DELETE FROM meal_items WHERE id = :mealItemId")
    suspend fun deleteMealItem(mealItemId: String)

    // Delete all meal items for a day
    @Query("DELETE FROM meal_items WHERE mealPlanId = :planId AND day = :day")
    suspend fun deleteMealItemsForDay(planId: String, day: String)

    // Transaction to insert a meal plan with its items
    @Transaction
    suspend fun insertMealPlanWithItems(mealPlan: MealPlanEntity, mealItems: List<MealItemEntity>) {
        insertMealPlan(mealPlan)
        if (mealItems.isNotEmpty()) {
            insertMealItems(mealItems)
        }
    }
}