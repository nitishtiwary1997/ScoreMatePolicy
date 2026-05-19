package com.nitish.cricketscoringapp.data.remote

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

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = auth.signOut()
}
