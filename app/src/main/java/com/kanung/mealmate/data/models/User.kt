package com.kanung.mealmate.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    var profileImageUrl: String = "",
    var notificationsEnabled: Boolean = true,
    var darkModeEnabled: Boolean = false
)