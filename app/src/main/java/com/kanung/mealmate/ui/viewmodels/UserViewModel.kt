package com.kanung.mealmate.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanung.mealmate.data.models.User
import com.kanung.mealmate.data.repositories.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Add these missing LiveData objects that ProfileFragment is trying to observe
    private val _recipeCount = MutableLiveData<Int>(0)
    val recipeCount: LiveData<Int> = _recipeCount

    private val _mealPlanCount = MutableLiveData<Int>(0)
    val mealPlanCount: LiveData<Int> = _mealPlanCount

    private val _completedShopsCount = MutableLiveData<Int>(0)
    val completedShopsCount: LiveData<Int> = _completedShopsCount

    init {
        loadUserProfile()
        loadUserStats()
    }

    fun loadUserProfile() {
        _isLoading.value = true
        userRepository.getCurrentUser { user ->
            viewModelScope.launch {
                if (user != null) {
                    _userProfile.value = user
                } else {
                    _error.value = "Failed to load profile."
                }
                _isLoading.value = false
            }
        }
    }

    fun loadUserStats() {
        userRepository.getRecipeCount { count ->
            _recipeCount.value = count
        }

        userRepository.getMealPlanCount { count ->
            _mealPlanCount.value = count
        }

        userRepository.getCompletedShopsCount { count ->
            _completedShopsCount.value = count
        }
    }

    fun updateUserProfile(updatedUser: User) {
        _isLoading.value = true
        userRepository.updateUserProfile(updatedUser.name!!, updatedUser.email!!) { success ->
            viewModelScope.launch {
                if (success) {
                    // Reload profile to get the updated data
                    loadUserProfile()
                } else {
                    _error.value = "Failed to update profile."
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        _isLoading.value = true
        userRepository.updateProfileImage(imageUri) { success ->
            viewModelScope.launch {
                if (success) {
                    loadUserProfile()
                } else {
                    _error.value = "Failed to update profile Image"
                    _isLoading.value = false
                }
            }
        }
    }

    fun signOut() {
        userRepository.logout()
        // Additional logic for sign-out if necessary (e.g., navigating to login screen)
    }
}