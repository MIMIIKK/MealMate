package com.kanung.mealmate.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.data.repositories.GroceryRepository
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "GroceryViewModel"
    private val repository = GroceryRepository(application)

    // LiveData for grocery items
    val groceryItems: LiveData<List<GroceryItem>> = repository.getAllGroceryItemsLiveData()
    val activeGroceryItems: LiveData<List<GroceryItem>> = repository.getActiveGroceryItems()
    val totalCost: LiveData<Double> = repository.getTotalCost()
    val itemCount: LiveData<Int> = repository.getItemCount()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _operationSuccessful = MutableLiveData<Boolean>()
    val operationSuccessful: LiveData<Boolean> = _operationSuccessful

    fun loadAllGroceryItems() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAllGroceryItems()
                // Items will be automatically loaded via LiveData
            } catch (e: Exception) {
                Log.e(TAG, "Error loading grocery items: ${e.message}")
                _error.value = e.message ?: "Failed to load grocery items"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addGroceryItem(item: GroceryItem) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.addGroceryItem(item)
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding grocery item: ${e.message}")
                _error.value = e.message ?: "Failed to add grocery item"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGroceryItem(item: GroceryItem) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.updateGroceryItem(item)
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating grocery item: ${e.message}")
                _error.value = e.message ?: "Failed to update grocery item"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateItemPurchasedStatus(itemId: String, isPurchased: Boolean) {
        viewModelScope.launch {
            try {
                repository.updatePurchasedStatus(itemId, isPurchased)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating purchased status: ${e.message}")
                _error.value = e.message ?: "Failed to update purchased status"
            }
        }
    }

    fun deleteGroceryItem(itemId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteGroceryItem(itemId)
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting grocery item: ${e.message}")
                _error.value = e.message ?: "Failed to delete grocery item"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCompletedItems() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.clearCompletedItems()
                _operationSuccessful.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing completed items: ${e.message}")
                _error.value = e.message ?: "Failed to clear completed items"
                _operationSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}