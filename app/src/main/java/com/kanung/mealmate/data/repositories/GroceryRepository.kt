package com.kanung.mealmate.data.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.kanung.mealmate.data.local.AppDatabase
import com.kanung.mealmate.data.local.entities.GroceryItemEntity
import com.kanung.mealmate.data.models.GroceryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroceryRepository(private val context: Context) {

    private val TAG = "GroceryRepository"
    private val auth = FirebaseAuth.getInstance()
    private val database = AppDatabase.getDatabase(context)
    private val groceryItemDao = database.groceryItemDao()

    // Get all grocery items as LiveData
    fun getAllGroceryItemsLiveData(): LiveData<List<GroceryItem>> {
        val userId = auth.currentUser?.uid ?: return androidx.lifecycle.MutableLiveData<List<GroceryItem>>().apply { value = emptyList() }
        return groceryItemDao.getAllGroceryItems(userId).map { entities ->
            entities.map { it.toGroceryItem() }
        }
    }

    // Get active (not purchased) grocery items
    fun getActiveGroceryItems(): LiveData<List<GroceryItem>> {
        val userId = auth.currentUser?.uid ?: return androidx.lifecycle.MutableLiveData<List<GroceryItem>>().apply { value = emptyList() }
        return groceryItemDao.getActiveGroceryItems(userId).map { entities ->
            entities.map { it.toGroceryItem() }
        }
    }

    // Get total cost
    fun getTotalCost(): LiveData<Double> {
        val userId = auth.currentUser?.uid ?: return androidx.lifecycle.MutableLiveData<Double>().apply { value = 0.0 }
        return groceryItemDao.getTotalCost(userId).map { it ?: 0.0 }
    }

    // Get item count
    fun getItemCount(): LiveData<Int> {
        val userId = auth.currentUser?.uid ?: return androidx.lifecycle.MutableLiveData<Int>().apply { value = 0 }
        return groceryItemDao.getItemCount(userId)
    }

    // Get all items (synchronously)
    suspend fun getAllGroceryItems(): List<GroceryItem> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext emptyList()
            val groceryEntities = groceryItemDao.getAllGroceryItemsSync(userId)
            return@withContext groceryEntities.map { it.toGroceryItem() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all grocery items: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Add a grocery item
    suspend fun addGroceryItem(item: GroceryItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val newItem = item.copy(userId = userId)
            groceryItemDao.insertGroceryItem(GroceryItemEntity.fromGroceryItem(newItem))
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding grocery item: ${e.message}")
            throw e
        }
    }

    // Update a grocery item
    suspend fun updateGroceryItem(item: GroceryItem): Boolean = withContext(Dispatchers.IO) {
        try {
            groceryItemDao.updateGroceryItem(GroceryItemEntity.fromGroceryItem(item))
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating grocery item: ${e.message}")
            throw e
        }
    }

    // Update purchased status
    suspend fun updatePurchasedStatus(itemId: String, isPurchased: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            groceryItemDao.updatePurchasedStatus(itemId, isPurchased)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating purchased status: ${e.message}")
            throw e
        }
    }

    // Delete a grocery item
    suspend fun deleteGroceryItem(itemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            groceryItemDao.deleteGroceryItemById(itemId)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting grocery item: ${e.message}")
            throw e
        }
    }

    // Clear completed items
    suspend fun clearCompletedItems(): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            groceryItemDao.deleteCompletedItems(userId)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing completed items: ${e.message}")
            throw e
        }
    }
}