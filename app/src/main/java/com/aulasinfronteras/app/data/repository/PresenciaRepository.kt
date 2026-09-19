package com.aulasinfronteras.app.data.repository

import com.aulasinfronteras.app.data.model.EstadoPresencia
import com.aulasinfronteras.app.data.model.Presencia
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 3.2 Presencia Orientada a Eventos ("Live Status Lite").
 *
 * El profesor actualiza su estado con un botón (sin WebSocket persistente).
 * El documento se guarda en /presencia/{profesorUid} y el envío del FCM Topic
 * ("materia_{materiaId}") normalmente se dispara desde una Cloud Function
 * "onWrite" escuchando esta colección (fuera del alcance del cliente Android).
 * El alumno solo escucha este documento vía snapshot() mientras la pantalla
 * está abierta (onStart/onStop), evitando procesos en segundo plano.
 */
@Singleton
class PresenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val coleccion = firestore.collection("presencia")

    suspend fun actualizarEstado(
        profesorUid: String,
        profesorNombre: String,
        materiaId: String,
        materiaNombre: String,
        estado: EstadoPresencia,
        ubicacion: String,
        enlaceVirtual: String
    ): Result<Unit> = try {
        val presencia = Presencia(
            profesorUid = profesorUid,
            profesorNombre = profesorNombre,
            materiaId = materiaId,
            materiaNombre = materiaNombre,
            estado = estado,
            ubicacion = ubicacion,
            enlaceVirtual = enlaceVirtual,
            actualizadoEn = Timestamp.now()
        )
        coleccion.document(profesorUid).set(presencia).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Observa la presencia de un profesor puntual (pantalla del alumno). */
    fun observarPresenciaDeProfesor(profesorUid: String): Flow<Presencia?> = callbackFlow {
        val registro = coleccion.document(profesorUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Presencia::class.java))
            }
        awaitClose { registro.remove() }
    }

    /** Observa la presencia de todos los profesores de una lista de materias del alumno. */
    fun observarPresenciaPorMaterias(materiaIds: List<String>): Flow<List<Presencia>> = callbackFlow {
        val consulta = if (materiaIds.isEmpty()) {
            coleccion // Ver todos si no tiene materias asignadas aún
        } else {
            coleccion.whereIn("materiaId", materiaIds.take(30))
        }
        
        val registro = consulta
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Presencia::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }
}
