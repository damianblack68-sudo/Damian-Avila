package com.example.service

import android.content.Context
import android.util.Log
import com.example.R
import com.example.engine.OnyxNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * OnyxFirebaseMessagingService:
 * Servicio para procesar notificaciones push en tiempo real vía Firebase Cloud Messaging (FCM).
 * Maneja recepción de mensajes en segundo plano y primer plano, y sincroniza el token del dispositivo.
 */
class OnyxFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "OnyxFCMService"
        private const val PREFS_NAME = "onyx_fcm_prefs"
        private const val KEY_FCM_TOKEN = "key_fcm_token"

        /**
         * Obtiene el token FCM actual del dispositivo y lo asocia al usuario autenticado en Firestore.
         * También suscribe el cliente al canal temático ejecutivo 'onyx_broadcast'.
         */
        fun syncDeviceFcmToken(context: Context) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Error al recuperar token de registro FCM", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d(TAG, "Token FCM del dispositivo obtenido: $token")
                saveTokenLocally(context, token)
                syncTokenToFirestore(context, token)
            }

            // Suscribir a tópicos de cambios de la app, directivas y avisos proactivos
            FirebaseMessaging.getInstance().subscribeToTopic("onyx_broadcast")
            FirebaseMessaging.getInstance().subscribeToTopic("onyx_app_updates")
            FirebaseMessaging.getInstance().subscribeToTopic("onyx_proactive_assistant")
        }

        private fun saveTokenLocally(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        }

        fun getSavedToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_FCM_TOKEN, null)
        }

        private fun syncTokenToFirestore(context: Context, token: String) {
            val currentUser = Firebase.auth.currentUser ?: return
            try {
                val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
                val userRef = db.collection("users").document(currentUser.uid)
                val tokenData = mapOf(
                    "fcmToken" to token,
                    "fcmUpdatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(tokenData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Token FCM sincronizado en Firestore para usuario ${currentUser.uid}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Fallo al sincronizar token FCM en Firestore: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción sincronizando token FCM: ${e.message}")
            }
        }
    }

    /**
     * Llamado cuando se genera un nuevo token FCM o cuando expira el token previo.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM generado: $token")
        saveTokenLocally(applicationContext, token)
        syncTokenToFirestore(applicationContext, token)
    }

    /**
     * Llamado cuando se recibe un mensaje mientras la app está en primer o segundo plano.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        // 1. Extraer título y contenido (desde notificación o data payload)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Onyx IA • Notificación Ejecutiva"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "Nueva directiva o aviso en tiempo real."

        val channelType = remoteMessage.data["channel"] ?: "executive"
        val channelId = when (channelType) {
            "messages" -> OnyxNotificationHelper.CHANNEL_REALTIME_MESSAGES
            "creation" -> OnyxNotificationHelper.CHANNEL_APP_CREATION
            else -> OnyxNotificationHelper.CHANNEL_EXECUTIVE_ALERTS
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        // 2. Despachar notificación local al sistema
        OnyxNotificationHelper.sendRealtimeNotification(
            context = applicationContext,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            message = body,
            intentAction = remoteMessage.data["action"]
        )
    }
}
