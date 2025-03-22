package com.kanung.mealmate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class GroceryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val category: String = "",
    val isPurchased: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        val CATEGORIES = listOf(
            "Produce", "Dairy", "Meat", "Bakery", "Frozen",
            "Pantry", "Beverages", "Snacks", "Household", "Other"
        )
    }
}