package com.kanung.mealmate.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.kanung.mealmate.R
import com.kanung.mealmate.services.AuthenticationService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var signupText: TextView
    private lateinit var loadingIndicator: ProgressBar

    private val authService = AuthenticationService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if user is already logged in
        if (authService.getCurrentUser() != null) {
            navigateToHomePage()
            return
        }

        // Initialize views
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        forgotPasswordText = findViewById(R.id.tvForgotPassword)
        signupText = findViewById(R.id.tvRegister)

        // Login Button Click Listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Forgot Password Click Listener
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Signup Text Click Listener
        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailEditText.error = "Email cannot be empty"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password cannot be empty"
                false
            }
            else -> true
        }
    }

    private fun performLogin(email: String, password: String) {
        // Show loading indicator if it exists
        try {
            loadingIndicator.visibility = View.VISIBLE
        } catch (e: Exception) {
            // Handle case where loading indicator isn't in layout
        }

        loginButton.isEnabled = false

        lifecycleScope.launch {
            authService.login(email, password).onSuccess {
                navigateToHomePage()
            }.onFailure {
                Toast.makeText(this@LoginActivity, "Login failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            try {
                loadingIndicator.visibility = View.GONE
            } catch (e: Exception) {
                // Handle case where loading indicator isn't in layout
            }

            loginButton.isEnabled = true
        }
    }

    private fun navigateToHomePage() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}