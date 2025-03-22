package com.kanung.mealmate.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kanung.mealmate.data.models.Ingredient

class IngredientListConverter {
    @TypeConverter
    fun fromString(value: String): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<Ingredient>): String {
        return Gson().toJson(list)
    }
}