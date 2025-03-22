package com.kanung.mealmate.data.utils

import android.content.Context
import android.content.SharedPreferences
import com.kanung.mealmate.data.models.User
import com.google.gson.Gson

class UserPreferencesManager(context: Context) {

    companion object {
        private const val PREF_NAME = "MealMatePrefs"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER_DATA, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    fun updateUserPreference(key: String, value: Any) {
        val user = getUser() ?: return

        when (key) {
            "notificationsEnabled" -> user.notificationsEnabled = value as Boolean
            "darkModeEnabled" -> user.darkModeEnabled = value as Boolean
            "profileImageUrl" -> user.profileImageUrl = value as String
        }

        saveUser(user)
    }
}