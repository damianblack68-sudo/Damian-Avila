package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseAuthHelper {

    private const val TAG = "FirebaseAuthHelper"
    const val ADMIN_EMAIL = "damianblack68@gmail.com"

    /**
     * Map FirebaseUser to Onyx UserProfile domain model with admin detection.
     */
    fun mapFirebaseUserToProfile(firebaseUser: FirebaseUser): UserProfile {
        val email = firebaseUser.email?.trim()?.lowercase() ?: "usuario@onyx.ai"
        val isAdmin = email == ADMIN_EMAIL
        val baseName = firebaseUser.displayName?.trim()?.ifBlank { null }
            ?: email.substringBefore("@")
        val username = if (isAdmin) "damian_avila_admin" else email.substringBefore("@").replace(".", "_")

        return if (isAdmin) {
            UserProfile(
                email = ADMIN_EMAIL,
                username = "damian_avila_admin",
                displayName = "Damian Ávila (Creador & Admin)",
                role = UserRole.ADMIN_CREATOR,
                subscription = SubscriptionTier.EVOLUTION_1000,
                isVerified = true,
                qrCodeId = "ONX-DAMIAN-ADMIN-PASSPORT"
            )
        } else {
            UserProfile(
                email = email,
                username = username,
                displayName = baseName,
                role = UserRole.STANDARD_USER,
                subscription = SubscriptionTier.FREE,
                isVerified = false,
                qrCodeId = "ONX-$username"
            )
        }
    }

    /**
     * Guarda y sincroniza el perfil del usuario autenticado en su documento privado de Firestore (/users/{uid})
     */
    suspend fun syncUserProfileToFirestore(context: Context, profile: UserProfile, uid: String) {
        try {
            val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            val userDoc = db.collection("users").document(uid)
            val data = mapOf(
                "email" to profile.email,
                "username" to profile.username,
                "role" to profile.role.name,
                "subscription" to profile.subscription.name,
                "qrCodeId" to profile.qrCodeId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(data, SetOptions.merge()).await()
            Log.d(TAG, "User profile synced securely to Firestore for uid=$uid")
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing user profile to Firestore: ${e.message}")
        }
    }

    /**
     * Intento silencioso de inicio de sesión con Google al abrir la aplicación.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: (UserProfile) -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        val currentFirebaseUser = Firebase.auth.currentUser
        if (currentFirebaseUser != null) {
            val profile = mapFirebaseUserToProfile(currentFirebaseUser)
            onAuthSuccess(profile)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id not found for auto sign-in")
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val profile = mapFirebaseUserToProfile(user)
                        syncUserProfileToFirestore(context, profile, user.uid)
                        onAuthSuccess(profile)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Inicio de sesión interactivo con Google Sign-In mediante CredentialManager.
     */
    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        onAuthSuccess: (UserProfile) -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Configuración de Google Sign-In ausente: default_web_client_id no encontrado.")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val profile = mapFirebaseUserToProfile(user)
                        syncUserProfileToFirestore(activity, profile, user.uid)
                        onAuthSuccess(profile)
                    } else {
                        onAuthError("Error: No se obtuvo usuario autenticado de Firebase.")
                    }
                } else {
                    onAuthError("Tipo de credencial no compatible.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelado o descartado por el usuario: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Error en Google Sign-In: ${e.message}", e)
                onAuthError(e.localizedMessage ?: "Error al iniciar sesión con Google")
            }
        }
    }

    /**
     * Cierra la sesión tanto en Firebase Auth como en CredentialManager.
     */
    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Error limpiando estado de credenciales: ${e.message}", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
