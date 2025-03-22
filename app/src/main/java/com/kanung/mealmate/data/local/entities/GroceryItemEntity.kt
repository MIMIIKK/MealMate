package com.kanung.mealmate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kanung.mealmate.data.models.GroceryItem
import java.util.UUID

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val category: String = "",
    val isPurchased: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toGroceryItem(): GroceryItem {
        return GroceryItem(
            id = id,
            name = name,
            quantity = quantity,
            price = price,
            category = category,
            isPurchased = isPurchased,
            userId = userId,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromGroceryItem(groceryItem: GroceryItem): GroceryItemEntity {
            return GroceryItemEntity(
                id = groceryItem.id,
                name = groceryItem.name,
                quantity = groceryItem.quantity,
                price = groceryItem.price,
                category = groceryItem.category,
                isPurchased = groceryItem.isPurchased,
                userId = groceryItem.userId,
                createdAt = groceryItem.createdAt
            )
        }
    }
}