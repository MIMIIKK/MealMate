package com.kanung.mealmate.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kanung.mealmate.services.AuthenticationService

class MainActivity : AppCompatActivity() {
    private val authService = AuthenticationService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        if (authService.isUserLoggedIn()) {
            // User is logged in, navigate to home
            navigateToHome()
        } else {
            // User is not logged in, navigate to login
            navigateToLogin()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}