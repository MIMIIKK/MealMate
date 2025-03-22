package com.kanung.mealmate.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kanung.mealmate.data.local.entities.GroceryItemEntity

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items WHERE userId = :userId ORDER BY category ASC, name ASC")
    fun getAllGroceryItems(userId: String): LiveData<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE userId = :userId ORDER BY category ASC, name ASC")
    suspend fun getAllGroceryItemsSync(userId: String): List<GroceryItemEntity>

    @Query("SELECT * FROM grocery_items WHERE userId = :userId AND isPurchased = 0 ORDER BY category ASC, name ASC")
    fun getActiveGroceryItems(userId: String): LiveData<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity)

    @Update
    suspend fun updateGroceryItem(item: GroceryItemEntity)

    @Query("UPDATE grocery_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchasedStatus(id: String, isPurchased: Boolean)

    @Delete
    suspend fun deleteGroceryItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteGroceryItemById(id: String)

    @Query("DELETE FROM grocery_items WHERE userId = :userId AND isPurchased = 1")
    suspend fun deleteCompletedItems(userId: String)

    @Query("SELECT SUM(price * quantity) FROM grocery_items WHERE userId = :userId")
    fun getTotalCost(userId: String): LiveData<Double?>

    @Query("SELECT COUNT(*) FROM grocery_items WHERE userId = :userId")
    fun getItemCount(userId: String): LiveData<Int>
}