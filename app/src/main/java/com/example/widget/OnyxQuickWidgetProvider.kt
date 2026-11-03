package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.ui.screens.voice.OnyxQuickVoiceActivity

/**
 * App Widget de Android para Onyx IA.
 * Permite a los usuarios tener accesos directos e interactivos en su pantalla de inicio:
 * 1. Botón circular con pulso rojo: "HABLAR" directo con Onyx IA flotante (OnyxQuickVoiceActivity)
 * 2. Botón de acceso a Chat & Razonamiento en tiempo real
 * 3. Botón de acceso directo a Workspace & Tareas
 */
class OnyxQuickWidgetProvider : AppWidgetProvider() {

    companion object {
        const val REQUEST_CODE_VOICE = 1001
        const val REQUEST_CODE_CHAT = 1002
        const val REQUEST_CODE_WORKSPACE = 1003

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, OnyxQuickWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, OnyxQuickWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_onyx_quick_access)

        // 1. Acceso Rápido por Voz: Abre OnyxQuickVoiceActivity directamente (HUD flotante sobre el inicio)
        val voiceIntent = Intent(context, OnyxQuickVoiceActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(OnyxQuickVoiceActivity.EXTRA_AUTO_START_RECORDING, true)
        }
        val voicePendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_VOICE,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_voice, voicePendingIntent)

        // 2. Acceso Directo al Chat de Onyx IA
        val chatIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NAV_TAB, 0)
        }
        val chatPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_CHAT,
            chatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_chat, chatPendingIntent)

        // 3. Acceso Directo a Workspace y Tareas
        val workspaceIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NAV_TAB, 1)
        }
        val workspacePendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_WORKSPACE,
            workspaceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_workspace, workspacePendingIntent)

        // Actualizar el widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
