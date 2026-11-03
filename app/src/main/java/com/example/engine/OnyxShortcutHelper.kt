package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

/**
 * OnyxShortcutHelper:
 * Permite instalar el acceso directo de Onyx Open en la pantalla de inicio del teléfono
 * con 1 solo toque (Pinned Shortcut) y compartir el enlace de instalación directa del APK / Web PWA.
 */
object OnyxShortcutHelper {

    const val DIRECT_APK_URL = "https://ais-pre-ncyfh5jjg5kv6apjny7tad-679952420096.us-west2.run.app"

    fun pinOnyxHomeScreenShortcut(context: Context) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "onyx_pinned_shortcut")
                .setShortLabel("Onyx Open")
                .setLongLabel("Onyx Open • Acceso Directo Inmediato")
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(launchIntent)
                .build()

            val success = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
            if (success) {
                Toast.makeText(context, "Solicitud enviada: Acepta para fijar el acceso directo en tu pantalla de inicio", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "No se pudo anclar el acceso directo automáticamente", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Tu lanzador de inicio no admite fijar accesos directos automáticos. Puedes arrastrar el icono desde el menú de apps.", Toast.LENGTH_LONG).show()
        }
    }

    fun shareDirectApkLink(context: Context) {
        val shareText = buildString {
            appendLine("🚀 Descarga e Instalación Directa de Onyx Open APK:")
            appendLine(DIRECT_APK_URL)
            appendLine()
            appendLine("• Instalación en 1 toque en tu teléfono.")
            appendLine("• Si entras desde el navegador web, verás la opción 'Instalar App' arriba junto a la barra de URL.")
            appendLine("• Inteligencia Artificial ejecutiva, privacidad absoluta y acceso offline protegido.")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Descargar Onyx Open APK")
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir Instalador Directo Onyx Open")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
