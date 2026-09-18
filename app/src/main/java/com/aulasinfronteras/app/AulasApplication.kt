package com.aulasinfronteras.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.firestoreSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AulasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configurarCachePersistenteDeFirestore()
        crearCanalesDeNotificacion()
    }

    /**
     * 4. Persistencia Offline: sustituye completamente a Room y WorkManager.
     * Con estas líneas, Firestore guarda en caché local automáticamente
     * y las consultas previas quedan disponibles sin conexión a internet.
     */
    private fun configurarCachePersistenteDeFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = firestoreSettings {
            setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        }
    }

    /** Canales requeridos por FCM: Noticias, Cambios de Aula y Urgente (3.3). */
    private fun crearCanalesDeNotificacion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)

        val canalNoticias = NotificationChannel(
            getString(R.string.canal_noticias_id),
            getString(R.string.canal_noticias_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val canalCambiosAula = NotificationChannel(
            getString(R.string.canal_cambios_aula_id),
            getString(R.string.canal_cambios_aula_nombre),
            NotificationManager.IMPORTANCE_HIGH
        )
        val canalUrgente = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            getString(R.string.canal_urgente_nombre),
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannel(canalNoticias)
        manager.createNotificationChannel(canalCambiosAula)
        manager.createNotificationChannel(canalUrgente)
    }
}
