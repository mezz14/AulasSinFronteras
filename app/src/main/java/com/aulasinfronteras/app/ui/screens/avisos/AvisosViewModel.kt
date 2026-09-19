package com.aulasinfronteras.app.ui.screens.avisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasinfronteras.app.data.model.Aviso
import com.aulasinfronteras.app.data.model.CanalAviso
import com.aulasinfronteras.app.data.model.Usuario
import com.aulasinfronteras.app.data.repository.AvisoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvisosUiState(
    val avisos: List<Aviso> = emptyList(),
    val enviando: Boolean = false
)

/**
 * 3.3 Notificaciones Push e In-App.
 * El Administrador crea avisos institucionales (materiaId vacío);
 * el Profesor crea avisos de materia. Ambos quedan disponibles de
 * inmediato en la lista in-app gracias a los listeners de Firestore,
 * y además viajan por FCM Topics categorizados por canal.
 */
@HiltViewModel
class AvisosViewModel @Inject constructor(
    private val avisoRepository: AvisoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvisosUiState())
    val uiState: StateFlow<AvisosUiState> = _uiState.asStateFlow()

    fun inicializar(usuario: Usuario) {
        viewModelScope.launch {
            avisoRepository.suscribirseACanalesGenerales()
            val flujo = when {
                usuario.isAdmin -> avisoRepository.observarAvisosInstitucionales()
                usuario.isTeacher -> {
                    // El profesor ve institucionales, los de sus materias Y los que él mismo creó
                    // Para simplificar y asegurar visibilidad, usamos observarAvisosPorMaterias
                    // pero si la lista es vacía, el repo ya asegura ver los institucionales.
                    avisoRepository.observarAvisosPorMaterias(usuario.materiasImpartidas)
                }
                else -> avisoRepository.observarAvisosPorMaterias(usuario.materiasMatriculadas)
            }
            flujo.collect { avisos -> _uiState.value = _uiState.value.copy(avisos = avisos) }
        }
    }

    fun crearAviso(
        titulo: String,
        contenido: String,
        canal: CanalAviso,
        materiaId: String,
        usuario: Usuario,
        alTerminar: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true)
            val aviso = Aviso(
                titulo = titulo,
                contenido = contenido,
                canal = canal,
                materiaId = materiaId,
                creadoPorUid = usuario.uid,
                creadoPorNombre = usuario.nombre
            )
            val resultado = avisoRepository.crearAviso(aviso)
            _uiState.value = _uiState.value.copy(enviando = false)
            alTerminar(resultado)
        }
    }
}
