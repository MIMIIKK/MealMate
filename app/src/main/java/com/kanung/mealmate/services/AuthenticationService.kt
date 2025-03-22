package com.kanung.mealmate.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthenticationService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Attempts to log in a user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [Result] object indicating the success or failure of the login attempt.
     * If successful, the [Result] will contain the [FirebaseUser].
     * If failed, it will contain the exception that occurred.
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [Result] object indicating the success or failure of the registration attempt.
     * If successful, the [Result] will contain the [FirebaseUser].
     * If failed, it will contain the exception that occurred.
     */
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The [FirebaseUser] if a user is currently logged in, otherwise null.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Signs out the currently authenticated user.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return True if a user is logged in, false otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A [Result] object indicating the success or failure of the operation.
     * If successful, the [Result] will contain null.
     * If failed, it will contain the exception that occurred.
     */
    suspend fun resetPassword(email: String): Result<Void?> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}