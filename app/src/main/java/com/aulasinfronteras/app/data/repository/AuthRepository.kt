package com.aulasinfronteras.app.data.repository

import com.aulasinfronteras.app.data.model.RolUsuario
import com.aulasinfronteras.app.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maneja login/registro con Firebase Auth y la lectura del perfil
 * (con su rol) desde la colección "usuarios" en Firestore.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val usuarioActualUid: String?
        get() = auth.currentUser?.uid

    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("No se pudo obtener el UID del usuario")
        Result.success(obtenerPerfil(uid))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun registrarUsuario(
        email: String,
        password: String,
        nombre: String,
        rol: RolUsuario
    ): Result<Usuario> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("No se pudo crear el usuario")
        val usuario = Usuario(uid = uid, nombre = nombre, email = email, rol = rol)
        firestore.collection("usuarios").document(uid).set(
            mapOf(
                "nombre" to usuario.nombre,
                "email" to usuario.email,
                "rol" to usuario.rol.name,
                "materiasMatriculadas" to usuario.materiasMatriculadas,
                "materiasImpartidas" to usuario.materiasImpartidas
            )
        ).await()
        Result.success(usuario)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerPerfil(uid: String): Usuario {
        val doc = firestore.collection("usuarios").document(uid).get().await()
        return Usuario(
            uid = uid,
            nombre = doc.getString("nombre") ?: "",
            email = doc.getString("email") ?: "",
            rol = RolUsuario.fromString(doc.getString("rol")),
            materiasMatriculadas = doc.get("materiasMatriculadas") as? List<String> ?: emptyList(),
            materiasImpartidas = doc.get("materiasImpartidas") as? List<String> ?: emptyList()
        )
    }

    fun cerrarSesion() = auth.signOut()
}
