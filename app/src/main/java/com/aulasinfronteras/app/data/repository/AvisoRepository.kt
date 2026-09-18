package com.aulasinfronteras.app.data.repository

import com.aulasinfronteras.app.data.model.Aviso
import com.aulasinfronteras.app.data.model.CanalAviso
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 3.3 Notificaciones Push e In-App.
 * Los avisos se guardan en Firestore (para la lista in-app con snapshots())
 * y además se envían por FCM Topics categorizados por canal para el push nativo.
 * El envío real del push (Admin SDK) se hace normalmente vía Cloud Function
 * al detectar la creación del documento; aquí dejamos el hook de suscripción
 * a topics para que el cliente reciba las notificaciones.
 */
@Singleton
class AvisoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    private val coleccion = firestore.collection("avisos")

    fun observarAvisosInstitucionales(): Flow<List<Aviso>> = callbackFlow {
        val registro = coleccion
            .whereEqualTo("materiaId", "")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Aviso::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    fun observarAvisosPorMaterias(materiaIds: List<String>): Flow<List<Aviso>> = callbackFlow {
        if (materiaIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registro = coleccion
            .whereIn("materiaId", materiaIds.take(30))
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Aviso::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun crearAviso(aviso: Aviso): Result<Unit> = try {
        val ref = coleccion.document()
        ref.set(aviso.copy(id = ref.id)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Suscribe al usuario al topic de una materia, p.ej. "materia_MAT101". */
    suspend fun suscribirseATopicDeMateria(materiaId: String) {
        messaging.subscribeToTopic("materia_$materiaId").await()
    }

    suspend fun desuscribirseDeTopicDeMateria(materiaId: String) {
        messaging.unsubscribeFromTopic("materia_$materiaId").await()
    }

    /** Suscribe a los canales generales descritos en el documento (Noticias / Cambios de Aula / Urgente). */
    suspend fun suscribirseACanalesGenerales() {
        CanalAviso.entries.forEach { canal ->
            messaging.subscribeToTopic(canal.name.lowercase()).await()
        }
    }
}
