package com.example.engine

import android.content.Context
import android.util.Log
import com.example.R
import com.example.domain.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * OnyxProactiveAlertManager:
 * Gestiona alertas proactivas del asistente inteligente Onyx IA y notificaciones en tiempo real
 * de cambios en la aplicación, nuevas versiones o directivas del workspace.
 */
object OnyxProactiveAlertManager {

    private const val TAG = "OnyxProactiveAlerts"
    private const val PREFS_NAME = "onyx_proactive_prefs"
    private const val KEY_LAST_NOTIFIED_UPDATE_ID = "last_notified_update_id"

    private var updatesListener: ListenerRegistration? = null

    /**
     * Inicia la escucha activa de cambios de la aplicación desde Firestore en tiempo real.
     * Si se detecta un nuevo registro de actualización o versión, despacha una notificación push proactiva.
     */
    fun startListeningForAppUpdates(context: Context) {
        if (updatesListener != null) return

        try {
            val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            updatesListener = db.collection("app_updates")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Error escuchando app_updates: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val latestDoc = snapshots.documents.first()
                        val updateId = latestDoc.id
                        val title = latestDoc.getString("title") ?: "Nueva Actualización"
                        val version = latestDoc.getString("version") ?: "1.0"
                        val summary = latestDoc.getString("summary") ?: "Se han añadido mejoras y nuevas funciones a Onyx Open."

                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val lastNotifiedId = prefs.getString(KEY_LAST_NOTIFIED_UPDATE_ID, null)

                        // Si es un cambio nuevo que no se ha notificado aún
                        if (lastNotifiedId != updateId) {
                            prefs.edit().putString(KEY_LAST_NOTIFIED_UPDATE_ID, updateId).apply()

                            // Notificar proactivamente al usuario del cambio en la app
                            OnyxNotificationHelper.sendRealtimeNotification(
                                context = context,
                                notificationId = 10091,
                                channelId = OnyxNotificationHelper.CHANNEL_APP_CREATION,
                                title = "🚀 Actualización Onyx Open ($version)",
                                message = "$title: $summary",
                                intentAction = "ACTION_OPEN_UPDATES"
                            )
                        }
                    }
                }
            Log.d(TAG, "Escucha en tiempo real de app_updates iniciada con éxito")
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando escucha de app_updates: ${e.message}")
        }
    }

    fun stopListening() {
        updatesListener?.remove()
        updatesListener = null
    }

    /**
     * Envía una alerta proactiva del asistente (por ejemplo, directivas de tareas,
     * recordatorios de asistencia docente o avisos ejecutivos).
     */
    fun sendProactiveAssistantAlert(
        context: Context,
        title: String,
        message: String,
        channelId: String = OnyxNotificationHelper.CHANNEL_EXECUTIVE_ALERTS
    ) {
        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        OnyxNotificationHelper.sendRealtimeNotification(
            context = context,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            message = message
        )
    }

    /**
     * Publica un cambio o actualización de la app en Firestore para que sea transmitido
     * en tiempo real a todos los usuarios del sistema.
     */
    fun publishAppChangeNotice(
        context: Context,
        title: String,
        version: String,
        summary: String,
        onComplete: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
                val updateData = mapOf(
                    "title" to title,
                    "version" to version,
                    "summary" to summary,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("app_updates").add(updateData).await()

                // Despachar inmediatamente notificación local
                sendProactiveAssistantAlert(
                    context = context,
                    title = "🚀 Actualización Onyx Open ($version)",
                    message = "$title: $summary",
                    channelId = OnyxNotificationHelper.CHANNEL_APP_CREATION
                )

                launch(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error publicando actualización de app: ${e.message}")
                // Fallback: Despachar notificación local directa
                sendProactiveAssistantAlert(
                    context = context,
                    title = "🚀 Actualización Onyx Open ($version)",
                    message = "$title: $summary",
                    channelId = OnyxNotificationHelper.CHANNEL_APP_CREATION
                )
                launch(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
}
