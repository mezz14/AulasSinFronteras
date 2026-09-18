package com.aulasinfronteras.app.ui.screens.presencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasinfronteras.app.data.model.EstadoPresencia
import com.aulasinfronteras.app.data.model.Presencia
import com.aulasinfronteras.app.data.model.Usuario
import com.aulasinfronteras.app.data.repository.PresenciaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PresenciaUiState(
    val estadoActual: EstadoPresencia = EstadoPresencia.DESCONECTADO,
    val ubicacion: String = "",
    val enlaceVirtual: String = "",
    val guardando: Boolean = false,
    // Vista del alumno: presencia de los profesores de sus materias.
    val presenciasDeProfesores: List<Presencia> = emptyList()
)

/**
 * 3.2 Presencia Orientada a Eventos ("Live Status Lite").
 * El profesor cambia su estado con un botón (sin socket persistente); el alumno
 * solo escucha mientras la pantalla está abierta gracias a que este ViewModel
 * arranca el listener en inicializarComoAlumno() y Compose lo cancela al salir
 * de la pantalla (WhileSubscribed vía collectAsStateWithLifecycle en la UI).
 */
@HiltViewModel
class PresenciaViewModel @Inject constructor(
    private val presenciaRepository: PresenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresenciaUiState())
    val uiState: StateFlow<PresenciaUiState> = _uiState.asStateFlow()

    fun cambiarEstado(
        usuario: Usuario,
        materiaId: String,
        materiaNombre: String,
        nuevoEstado: EstadoPresencia,
        ubicacion: String,
        enlaceVirtual: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            presenciaRepository.actualizarEstado(
                profesorUid = usuario.uid,
                profesorNombre = usuario.nombre,
                materiaId = materiaId,
                materiaNombre = materiaNombre,
                estado = nuevoEstado,
                ubicacion = ubicacion,
                enlaceVirtual = enlaceVirtual
            )
            _uiState.value = _uiState.value.copy(
                guardando = false,
                estadoActual = nuevoEstado,
                ubicacion = ubicacion,
                enlaceVirtual = enlaceVirtual
            )
        }
    }

    fun inicializarComoAlumno(usuario: Usuario) {
        viewModelScope.launch {
            presenciaRepository.observarPresenciaPorMaterias(usuario.materiasMatriculadas)
                .collect { lista ->
                    _uiState.value = _uiState.value.copy(presenciasDeProfesores = lista)
                }
        }
    }
}
