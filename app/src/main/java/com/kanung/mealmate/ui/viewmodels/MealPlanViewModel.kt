package com.kanung.mealmate.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kanung.mealmate.data.models.MealItem
import com.kanung.mealmate.data.models.MealPlan
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.data.repositories.MealPlanRepository
import com.kanung.mealmate.data.repositories.RecipeRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val mealPlanRepository = MealPlanRepository(application)
    private val recipeRepository = RecipeRepository(application)
    private val auth = FirebaseAuth.getInstance()

    // Get meal plans from repository
    val mealPlans: LiveData<List<MealPlan>> = mealPlanRepository.getAllMealPlansLiveData()

    private val _currentMealPlan = MutableLiveData<MealPlan?>()
    val currentMealPlan: LiveData<MealPlan?> = _currentMealPlan

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadRecipes()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeList = recipeRepository.getAllRecipes()
                _recipes.value = recipeList
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMealPlanById(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mealPlan = mealPlanRepository.getMealPlanById(planId)
                _currentMealPlan.value = mealPlan
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewWeeklyPlan(title: String, description: String, startDate: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                mealPlanRepository.createWeeklyPlan(title, description, startDate)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMealPlan(mealPlan: MealPlan) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = mealPlanRepository.updateMealPlan(mealPlan)
                if (success) {
                    _currentMealPlan.value = mealPlan
                    _error.value = null
                } else {
                    _error.value = "Failed to update meal plan"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMealPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = mealPlanRepository.deleteMealPlan(planId)
                if (success) {
                    if (_currentMealPlan.value?.id == planId) {
                        _currentMealPlan.value = null
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to delete meal plan"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMealToMealPlan(planId: String, recipeId: String, recipeName: String, day: String, mealType: String, servings: Int, notes: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mealItem = MealItem(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipeId,
                    recipeName = recipeName,
                    day = day,
                    mealType = mealType,
                    servings = servings,
                    notes = notes
                )

                val success = mealPlanRepository.addMealToMealPlan(planId, mealItem)
                if (success) {
                    // Refresh current meal plan
                    getMealPlanById(planId)
                    _error.value = null
                } else {
                    _error.value = "Failed to add meal to plan"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeMealFromMealPlan(planId: String, mealItemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = mealPlanRepository.removeMealFromMealPlan(mealItemId)
                if (success) {
                    // Refresh current meal plan
                    getMealPlanById(planId)
                    _error.value = null
                } else {
                    _error.value = "Failed to remove meal from plan"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipeById(recipeId: String): Recipe? {
        return recipes.value?.find { it.id == recipeId }
    }

    fun clearCurrentMealPlan() {
        _currentMealPlan.value = null
    }
}