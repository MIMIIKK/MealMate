package com.kanung.mealmate.data.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.kanung.mealmate.data.local.AppDatabase
import com.kanung.mealmate.data.local.entities.MealItemEntity
import com.kanung.mealmate.data.local.entities.MealPlanEntity
import com.kanung.mealmate.data.models.MealItem
import com.kanung.mealmate.data.models.MealPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MealPlanRepository(private val context: Context) {
    private val TAG = "MealPlanRepository"
    private val auth = FirebaseAuth.getInstance()
    private val database = AppDatabase.getDatabase(context)
    private val mealPlanDao = database.mealPlanDao()

    // Get all meal plans as LiveData
    fun getAllMealPlansLiveData(): LiveData<List<MealPlan>> {
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            return MutableLiveData<List<MealPlan>>().apply {
                value = emptyList()
            }
        }

        return mealPlanDao.getAllMealPlans(userId).map { entities ->
            entities.map { entity ->
                // Create a MealPlan with empty meals initially
                // Actual meal items will be loaded when viewing the plan details
                entity.toMealPlan(emptyList())
            }
        }
    }

    // Get a specific meal plan with all its meal items
    suspend fun getMealPlanById(planId: String): MealPlan? = withContext(Dispatchers.IO) {
        try {
            val mealPlanEntity = mealPlanDao.getMealPlanById(planId) ?: return@withContext null
            val mealItems = mealPlanDao.getMealItemsForPlan(planId)
            return@withContext mealPlanEntity.toMealPlan(mealItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meal plan by ID: ${e.message}")
            return@withContext null
        }
    }

    // Create a new meal plan
    suspend fun createMealPlan(mealPlan: MealPlan): String = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val planId = mealPlan.id.ifEmpty { UUID.randomUUID().toString() }

            val now = Date()
            val planEntity = MealPlanEntity(
                id = planId,
                userId = userId,
                title = mealPlan.title,
                description = mealPlan.description,
                startDate = mealPlan.startDate,
                endDate = mealPlan.endDate,
                createdAt = now,
                updatedAt = now
            )

            // Convert map of meals to a flat list
            val mealItemEntities = mealPlan.meals.flatMap { (day, items) ->
                items.map { mealItem ->
                    MealItemEntity.fromMealItem(
                        mealItem.copy(day = day),
                        planId
                    )
                }
            }

            mealPlanDao.insertMealPlanWithItems(planEntity, mealItemEntities)
            return@withContext planId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating meal plan: ${e.message}")
            throw e
        }
    }

    // Update an existing meal plan
    suspend fun updateMealPlan(mealPlan: MealPlan): Boolean = withContext(Dispatchers.IO) {
        try {
            val planEntity = MealPlanEntity(
                id = mealPlan.id,
                userId = mealPlan.userId,
                title = mealPlan.title,
                description = mealPlan.description,
                startDate = mealPlan.startDate,
                endDate = mealPlan.endDate,
                createdAt = mealPlan.createdAt ?: Date(),
                updatedAt = Date()
            )

            mealPlanDao.updateMealPlan(planEntity)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating meal plan: ${e.message}")
            throw e
        }
    }

    // Delete a meal plan
    suspend fun deleteMealPlan(planId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            mealPlanDao.deleteMealPlan(planId)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting meal plan: ${e.message}")
            throw e
        }
    }

    // Add a meal to a meal plan
    suspend fun addMealToMealPlan(
        planId: String,
        mealItem: MealItem
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val itemId = mealItem.id.ifEmpty { UUID.randomUUID().toString() }
            val mealItemEntity = MealItemEntity.fromMealItem(
                mealItem.copy(id = itemId),
                planId
            )

            mealPlanDao.insertMealItems(listOf(mealItemEntity))
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding meal to plan: ${e.message}")
            throw e
        }
    }

    // Remove a meal from a meal plan
    suspend fun removeMealFromMealPlan(mealItemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            mealPlanDao.deleteMealItem(mealItemId)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing meal from plan: ${e.message}")
            throw e
        }
    }

    // Create a new weekly meal plan with empty days
    suspend fun createWeeklyPlan(title: String, description: String, startDate: Date): String = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val planId = UUID.randomUUID().toString()

            // Calculate end date (7 days from start date)
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val endDate = calendar.time

            // Create day names for the week
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val mealsMap = mutableMapOf<String, List<MealItem>>()

            calendar.time = startDate
            for (i in 0..6) {
                val dayName = dayFormat.format(calendar.time)
                mealsMap[dayName] = emptyList()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val mealPlan = MealPlan(
                id = planId,
                userId = userId,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                meals = mealsMap,
                createdAt = Date(),
                updatedAt = Date()
            )

            return@withContext createMealPlan(mealPlan)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating weekly plan: ${e.message}")
            throw e
        }
    }

    // Get meal items for a specific plan
    suspend fun getMealItemsForPlan(planId: String): List<MealItemEntity> = withContext(Dispatchers.IO) {
        try {
            return@withContext mealPlanDao.getMealItemsForPlan(planId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meal items: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Get meal items for a specific day of a plan
    suspend fun getMealItemsForDay(planId: String, day: String): List<MealItemEntity> = withContext(Dispatchers.IO) {
        try {
            return@withContext mealPlanDao.getMealItemsForDay(planId, day)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meal items for day: ${e.message}")
            return@withContext emptyList()
        }
    }
}