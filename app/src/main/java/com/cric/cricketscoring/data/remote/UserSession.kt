package com.cric.cricketscoring.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(
    private val auth: FirebaseAuth
) {
    val isSignedIn: Boolean get() = auth.currentUser != null
    val userId: String get() = auth.currentUser?.uid ?: ""
    val userName: String get() = auth.currentUser?.displayName ?: ""
    val userEmail: String get() = auth.currentUser?.email ?: ""
    val phoneNumber: String get() = auth.currentUser?.phoneNumber ?: ""

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            syncUserProfile()
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    private suspend fun syncUserProfile() {
        val currentUser = auth.currentUser ?: return
        val profile = mapOf(
            "id" to currentUser.uid,
            "mobile" to (currentUser.phoneNumber ?: ""),
            "name" to (currentUser.displayName ?: "Scorer"),
            "email" to (currentUser.email ?: ""),
            "createdAt" to System.currentTimeMillis()
        )
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .set(profile, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (_: Exception) { }
    }

    fun signOut() = auth.signOut()
}
