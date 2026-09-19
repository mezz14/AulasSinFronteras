package com.aulasinfronteras.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class CanalAviso {
    NOTICIAS,
    CAMBIOS_AULA,
    URGENTE;

    companion object {
        fun fromString(valor: String?): CanalAviso = when (valor?.uppercase()) {
            "CAMBIOS_AULA" -> CAMBIOS_AULA
            "URGENTE" -> URGENTE
            else -> NOTICIAS
        }
    }
}

/**
 * Documento de la colección "avisos" en Firestore.
 * Puede ser institucional (materiaId vacío, creado por Admin)
 * o de materia (creado por un Profesor).
 */
data class Aviso(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val canal: CanalAviso = CanalAviso.NOTICIAS,
    val materiaId: String = "", // vacío = aviso institucional global
    val creadoPorUid: String = "",
    val creadoPorNombre: String = "",
    val fechaCreacion: Timestamp = Timestamp.now()
) {
    constructor() : this("", "", "", CanalAviso.NOTICIAS, "", "", "", Timestamp.now())
}
