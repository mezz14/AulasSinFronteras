package com.aulasinfronteras.app.data.model

/**
 * Roles soportados por la app. Se guarda como String en Firestore
 * en el campo "rol" del documento del usuario (colección "usuarios").
 */
enum class RolUsuario {
    ADMINISTRADOR,
    PROFESOR,
    ALUMNO;

    companion object {
        fun fromString(valor: String?): RolUsuario = when (valor?.uppercase()) {
            "ADMINISTRADOR", "ADMIN" -> ADMINISTRADOR
            "PROFESOR" -> PROFESOR
            else -> ALUMNO
        }
    }
}

/**
 * Representa el perfil de usuario almacenado en Firestore: /usuarios/{uid}
 */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: RolUsuario = RolUsuario.ALUMNO,
    // Solo aplica a ALUMNO: ids de materias en las que está matriculado.
    val materiasMatriculadas: List<String> = emptyList(),
    // Solo aplica a PROFESOR: ids de materias que imparte.
    val materiasImpartidas: List<String> = emptyList()
) {
    // Constructor vacío requerido por Firestore para deserializar con toObject()
    constructor() : this("", "", "", RolUsuario.ALUMNO, emptyList(), emptyList())

    val isAdmin: Boolean get() = rol == RolUsuario.ADMINISTRADOR
    val isTeacher: Boolean get() = rol == RolUsuario.PROFESOR
    val isStudent: Boolean get() = rol == RolUsuario.ALUMNO
}
