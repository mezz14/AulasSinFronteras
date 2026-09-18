package com.aulasinfronteras.app.data.model

import com.google.firebase.Timestamp

enum class EstadoPresencia(val etiqueta: String) {
    EN_CLASE("En clase"),
    EN_ASESORIA("En asesoría"),
    DISPONIBLE("Disponible"),
    DESCONECTADO("Desconectado");

    companion object {
        fun fromString(valor: String?): EstadoPresencia =
            entries.find { it.name == valor } ?: DESCONECTADO
    }
}

/**
 * Documento de la colección "presencia" en Firestore, indexado por
 * el uid del profesor: /presencia/{profesorUid}
 *
 * Al cambiar, el backend/cliente publica un mensaje al FCM topic
 * "materia_{materiaId}" para notificar a los alumnos suscritos
 * ("Live Status Lite" descrito en el documento de especificación).
 */
data class Presencia(
    val profesorUid: String = "",
    val profesorNombre: String = "",
    val materiaId: String = "",
    val materiaNombre: String = "",
    val estado: EstadoPresencia = EstadoPresencia.DESCONECTADO,
    val ubicacion: String = "", // aula física
    val enlaceVirtual: String = "",
    val actualizadoEn: Timestamp = Timestamp.now()
) {
    constructor() : this("", "", "", "", EstadoPresencia.DESCONECTADO, "", "", Timestamp.now())
}
