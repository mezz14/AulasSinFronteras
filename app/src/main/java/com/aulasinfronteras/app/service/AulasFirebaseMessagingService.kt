package com.aulasinfronteras.app.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.aulasinfronteras.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 3.3 Notificaciones Push e In-App.
 * Recibe los mensajes de FCM (avisos institucionales, cambios de aula,
 * urgentes y las alertas de "Live Status Lite" de presencia docente)
 * categorizados por canal para no sobrecargar al estudiante.
 */
class AulasFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo = message.notification?.title ?: message.data["titulo"] ?: "Aulas Sin Fronteras"
        val cuerpo = message.notification?.body ?: message.data["cuerpo"] ?: ""
        val canalId = message.data["canal"] ?: getString(R.string.default_notification_channel_id)

        mostrarNotificacion(titulo, cuerpo, canalId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // El token se puede enviar/guardar en /usuarios/{uid}.fcmToken si se
        // requiere mensajería dirigida a un dispositivo específico.
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, canalId: String) {
        val notificacion = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notificacion)
    }
}
