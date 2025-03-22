package com.kanung.mealmate.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kanung.mealmate.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val usersCollection = firestore.collection("users")

    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                Result.success(authResult.user != null)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun registerUser(user: User, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Create user in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                val userId = authResult.user?.uid ?: return@withContext Result.failure(Exception("User ID not found"))

                // Save user data in Firestore
                val userWithId = user.copy(id = userId)
                usersCollection.document(userId).set(userWithId).await()

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserData(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val document = usersCollection.document(userId).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("User data conversion failed"))
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, imageUrl: String?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mutableMapOf<String, Any>("name" to name)

                // Add image URL if available
                if (imageUrl != null) {
                    updates["photoUrl"] = imageUrl
                }

                usersCollection.document(userId).update(updates).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun uploadProfileImage(userId: String, imageFile: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val filename = "profile_${userId}_${UUID.randomUUID()}"
                val storageRef = storage.reference.child("profile_images/$filename")

                val uploadTask = storageRef.putFile(imageFile.toUri()).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun File.toUri() = android.net.Uri.fromFile(this)

    // Added methods to support ViewModel
    fun getCurrentUser(callback: (User?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            usersCollection.document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    callback(user)
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    fun updateUserProfile(name: String, email: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "email" to email
            )

            usersCollection.document(userId).update(updates)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else {
            callback(false)
        }
    }

    fun updateProfileImage(imageUri: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            usersCollection.document(userId).update("photoUrl", imageUri)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else {
            callback(false)
        }
    }

    fun getRecipeCount(callback: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    callback(documents.size())
                }
                .addOnFailureListener {
                    callback(0)
                }
        } else {
            callback(0)
        }
    }

    fun getMealPlanCount(callback: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("mealPlans")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    callback(documents.size())
                }
                .addOnFailureListener {
                    callback(0)
                }
        } else {
            callback(0)
        }
    }

    fun getCompletedShopsCount(callback: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("groceryLists")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener { documents ->
                    callback(documents.size())
                }
                .addOnFailureListener {
                    callback(0)
                }
        } else {
            callback(0)
        }
    }

    fun logout() {
        auth.signOut()
    }
}