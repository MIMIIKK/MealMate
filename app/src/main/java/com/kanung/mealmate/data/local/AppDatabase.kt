package com.kanung.mealmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kanung.mealmate.data.local.converters.DateConverters
import com.kanung.mealmate.data.local.converters.IngredientListConverter
import com.kanung.mealmate.data.local.converters.StringListConverter
import com.kanung.mealmate.data.local.dao.GroceryItemDao
import com.kanung.mealmate.data.local.dao.MealPlanDao
import com.kanung.mealmate.data.local.dao.RecipeDao
import com.kanung.mealmate.data.local.entities.GroceryItemEntity
import com.kanung.mealmate.data.local.entities.MealItemEntity
import com.kanung.mealmate.data.local.entities.MealPlanEntity
import com.kanung.mealmate.data.local.entities.RecipeEntity

@Database(
    entities = [
        RecipeEntity::class,
        GroceryItemEntity::class,
        MealPlanEntity::class,
        MealItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    DateConverters::class,
    StringListConverter::class,
    IngredientListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mealmate_database"
                )
                    .fallbackToDestructiveMigration() // This will handle migration by recreating tables
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}