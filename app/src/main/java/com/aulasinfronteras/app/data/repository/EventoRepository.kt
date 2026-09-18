package com.aulasinfronteras.app.data.repository

import com.aulasinfronteras.app.data.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 3.1 Calendario de Campus e Inserción de Eventos.
 * Consulta directa a Firestore (whereEqualTo) sin base de datos local intermedia.
 * Los listeners con snapshots() mantienen la UI actualizada en vivo (3.3).
 */
@Singleton
class EventoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val coleccion = firestore.collection("eventos")

    /** Todos los eventos, ordenados por fecha (uso típico: Administrador). */
    fun observarTodos(): Flow<List<Evento>> = callbackFlow {
        val registro = coleccion
            .orderBy("fechaInicio", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Evento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    /** Eventos filtrados por una lista de materias (uso típico: Alumno/Profesor). */
    fun observarPorMaterias(materiaIds: List<String>): Flow<List<Evento>> = callbackFlow {
        if (materiaIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        // Firestore permite hasta 30 valores en whereIn.
        val registro = coleccion
            .whereIn("materiaId", materiaIds.take(30))
            .orderBy("fechaInicio", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Evento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    /** Filtro puntual por una sola asignatura (usado por el filtro de UI del alumno). */
    fun observarPorMateria(materiaId: String): Flow<List<Evento>> = callbackFlow {
        val registro = coleccion
            .whereEqualTo("materiaId", materiaId)
            .orderBy("fechaInicio", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Evento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun crearEvento(evento: Evento): Result<Unit> = try {
        val ref = coleccion.document()
        coleccion.document(ref.id).set(evento.copy(id = ref.id)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun eliminarEvento(eventoId: String): Result<Unit> = try {
        coleccion.document(eventoId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
