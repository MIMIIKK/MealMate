package com.kanung.mealmate.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kanung.mealmate.R
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        nameEditText = findViewById(R.id.etName)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        loginText = findViewById(R.id.tvLogin)

        // Register button click listener
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                performRegistration(name, email, password)
            }
        }

        // Navigate to login page
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            name.isEmpty() -> {
                nameEditText.error = "Name cannot be empty"
                false
            }
            email.isEmpty() -> {
                emailEditText.error = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                passwordEditText.error = "Password must be at least 6 characters"
                false
            }
            password != confirmPassword -> {
                confirmPasswordEditText.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun performRegistration(name: String, email: String, password: String) {
        lifecycleScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserToFirestore(userId, name, email)
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String) {
        val user = hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "photoUrl" to ""  // Adding empty photoUrl for profile image
        )

        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToHomePage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}