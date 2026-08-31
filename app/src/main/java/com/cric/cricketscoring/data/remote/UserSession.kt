package com.cric.cricketscoring.data.remote

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME    = "app_prefs"
private const val KEY_GUEST_MODE = "guest_mode"

@Singleton
class UserSession @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    val isSignedIn: Boolean get() = auth.currentUser != null
    val userId: String get() = auth.currentUser?.uid ?: ""
    val userName: String get() = auth.currentUser?.displayName ?: ""
    val userEmail: String get() = auth.currentUser?.email ?: ""
    val phoneNumber: String get() = auth.currentUser?.phoneNumber ?: ""

    /** Persisted so a guest who continued past Login stays on Home after a process restart. */
    var isGuestMode: Boolean
        get() = prefs.getBoolean(KEY_GUEST_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_GUEST_MODE, value).apply()

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            isGuestMode = false
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

    fun signOut() {
        auth.signOut()
        isGuestMode = false
    }
}
