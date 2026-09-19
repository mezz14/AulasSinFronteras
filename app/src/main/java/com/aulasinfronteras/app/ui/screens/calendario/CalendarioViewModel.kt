package com.aulasinfronteras.app.ui.screens.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasinfronteras.app.data.model.Evento
import com.aulasinfronteras.app.data.model.Usuario
import com.aulasinfronteras.app.data.repository.EventoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarioUiState(
    val eventos: List<Evento> = emptyList(),
    val materiaSeleccionada: String? = null, // null = "todas"
    val materiasDisponibles: List<String> = emptyList(),
    val cargando: Boolean = true
)

/**
 * ViewModel único para las 3 vistas del calendario (Admin/Profesor/Alumno).
 * El propio Composable decide qué controles de edición mostrar según
 * usuario.isAdmin / isTeacher (sección 5: "una sola app con vistas modulares").
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val eventoRepository: EventoRepository
) : ViewModel() {

    private val filtroMateria = MutableStateFlow<String?>(null)
    private val eventosFuente = MutableStateFlow<List<Evento>>(emptyList())
    private val cargando = MutableStateFlow(true)
    private var usuarioActualUid: String? = null

    val uiState: StateFlow<CalendarioUiState> =
        combine(eventosFuente, filtroMateria, cargando) { eventos, filtro, estaCargando ->
            val filtrados = if (filtro == null) eventos else eventos.filter { it.materiaId == filtro }
            CalendarioUiState(
                eventos = filtrados,
                materiaSeleccionada = filtro,
                materiasDisponibles = eventos.map { it.materiaNombre }.distinct(),
                cargando = estaCargando
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarioUiState())

    /**
     * Debe llamarse una vez que se conoce el usuario logueado, para elegir
     * la consulta correcta: todos los eventos (Admin) o solo los de sus
     * materias (Profesor/Alumno) — 3.1 Suscripción por Asignatura.
     */
    fun inicializarParaUsuario(usuario: Usuario) {
        if (usuarioActualUid == usuario.uid) return
        usuarioActualUid = usuario.uid
        viewModelScope.launch {
            // Para asegurar que todos vean los eventos de campus y puedan filtrar,
            // observamos todos los eventos. La UI permite filtrar por materia.
            eventoRepository.observarTodos().collect { eventos ->
                eventosFuente.value = eventos
                cargando.value = false
            }
        }
    }

    fun seleccionarMateria(materiaId: String?) {
        filtroMateria.value = materiaId
    }

    fun crearEvento(evento: Evento, alTerminar: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            alTerminar(eventoRepository.crearEvento(evento))
        }
    }
}
