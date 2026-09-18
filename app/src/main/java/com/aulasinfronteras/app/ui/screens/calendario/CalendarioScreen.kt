package com.aulasinfronteras.app.ui.screens.calendario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aulasinfronteras.app.data.model.Evento
import com.aulasinfronteras.app.data.model.Usuario
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pantalla de calendario compartida por los 3 roles. El FAB de "nuevo evento"
 * solo se muestra si uiState.isAdmin (Admin: calendarios generales) — condicional
 * simple `if (usuario.isAdmin)` tal como describe la sección 5 del documento.
 */
@Composable
fun CalendarioScreen(
    usuario: Usuario,
    onCrearEvento: () -> Unit,
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuario.uid) {
        viewModel.inicializarParaUsuario(usuario)
    }

    Scaffold(
        floatingActionButton = {
            if (usuario.isAdmin) {
                FloatingActionButton(onClick = onCrearEvento) {
                    Icon(Icons.Default.Add, contentDescription = "Crear evento")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Calendario de Campus",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.materiasDisponibles.isNotEmpty()) {
                FiltroDeMaterias(
                    materias = uiState.materiasDisponibles,
                    seleccionada = uiState.materiaSeleccionada,
                    onSeleccion = viewModel::seleccionarMateria
                )
            }

            when {
                uiState.cargando -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.eventos.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("No hay eventos programados todavía.") }

                else -> ListaDeEventos(uiState.eventos)
            }
        }
    }
}

@Composable
private fun FiltroDeMaterias(
    materias: List<String>,
    seleccionada: String?,
    onSeleccion: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = seleccionada == null,
                onClick = { onSeleccion(null) },
                label = { Text("Todas") }
            )
        }
        items(materias) { materia ->
            FilterChip(
                selected = seleccionada == materia,
                onClick = { onSeleccion(materia) },
                label = { Text(materia) }
            )
        }
    }
}

@Composable
private fun ListaDeEventos(eventos: List<Evento>) {
    val formato = remember(eventos) { SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "MX")) }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(eventos, key = { it.id }) { evento ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(evento.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(evento.materiaNombre, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        formato.format(evento.fechaInicio.toDate()),
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (evento.aula.isNotBlank() || evento.enlaceVirtual.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(evento.aula.ifBlank { evento.enlaceVirtual })
                        }
                    }
                }
            }
        }
    }
}
