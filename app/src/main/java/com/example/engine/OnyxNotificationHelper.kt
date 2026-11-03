package com.example.engine

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * OnyxNotificationHelper:
 * Sistema de notificaciones en tiempo real para Onyx Open.
 * Despacha alertas ejecutivas, avisos de tareas, creación de apps en el emulador
 * y eventos del sistema sin comprometer datos privados del usuario.
 */
object OnyxNotificationHelper {

    const val CHANNEL_EXECUTIVE_ALERTS = "onyx_executive_alerts"
    const val CHANNEL_REALTIME_MESSAGES = "onyx_realtime_messages"
    const val CHANNEL_APP_CREATION = "onyx_app_creation"

    private var channelsInitialized = false

    fun initChannels(context: Context) {
        if (channelsInitialized || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de Alertas Ejecutivas
        val executiveChannel = NotificationChannel(
            CHANNEL_EXECUTIVE_ALERTS,
            "Directivas & Alertas Ejecutivas Onyx",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones prioritarias y respuestas de Onyx IA en tiempo real"
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
        }

        // Canal de Mensajería en Tiempo Real
        val messagesChannel = NotificationChannel(
            CHANNEL_REALTIME_MESSAGES,
            "Mensajería & Asistente en Tiempo Real",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de mensajes, contactos y sincronización privada"
            enableVibration(true)
        }

        // Canal de Creación de Aplicaciones
        val appCreationChannel = NotificationChannel(
            CHANNEL_APP_CREATION,
            "Estudio de Creación de Apps & Emulador",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas cuando una aplicación web/código está lista para probarse en el emulador"
            enableVibration(true)
        }

        notificationManager.createNotificationChannels(
            listOf(executiveChannel, messagesChannel, appCreationChannel)
        )
        channelsInitialized = true
    }

    fun sendRealtimeNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        intentAction: String? = null
    ) {
        initChannels(context)

        // Verificar permiso en Android 13+ (POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intentAction?.let { action = it }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFFF2A54.toInt()) // Onyx Red Primary
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Ignorar de forma segura si el usuario revocó el permiso
        }
    }
}
