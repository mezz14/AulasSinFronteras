package com.aulasinfronteras.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Documento de la colección "eventos" en Firestore.
 * Representa una clase, examen o actividad de campus.
 */
data class Evento(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val materiaId: String = "",
    val materiaNombre: String = "",
    val aula: String = "",
    val enlaceVirtual: String = "",
    val fechaInicio: Timestamp = Timestamp.now(),
    val fechaFin: Timestamp = Timestamp.now(),
    val creadoPorUid: String = ""
) {
    constructor() : this("", "", "", "", "", "", Timestamp.now(), Timestamp.now(), "")
}
