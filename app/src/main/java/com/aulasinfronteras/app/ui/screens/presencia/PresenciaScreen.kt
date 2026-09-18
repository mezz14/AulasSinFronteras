package com.aulasinfronteras.app.ui.screens.presencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aulasinfronteras.app.data.model.EstadoPresencia
import com.aulasinfronteras.app.data.model.Usuario

/**
 * Vista del PROFESOR: cambia su estado "en un clic" (sección 3.2).
 * Cada botón corresponde a uno de los tres estados descritos en el
 * documento de especificación: "En clase", "En asesoría", "Disponible".
 */
@Composable
fun PresenciaProfesorScreen(
    usuario: Usuario,
    materiaId: String,
    materiaNombre: String,
    viewModel: PresenciaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var ubicacion by remember { mutableStateOf("") }
    var enlace by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text("Mi estado de presencia", style = MaterialTheme.typography.titleLarge)
        Text(
            "Estado actual: ${uiState.estadoActual.etiqueta}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Aula física (opcional)") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = enlace,
            onValueChange = { enlace = it },
            label = { Text("Enlace virtual (opcional)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Column(modifier = Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(EstadoPresencia.EN_CLASE, EstadoPresencia.EN_ASESORIA, EstadoPresencia.DISPONIBLE)
                .forEach { estado ->
                    Button(
                        onClick = {
                            viewModel.cambiarEstado(
                                usuario = usuario,
                                materiaId = materiaId,
                                materiaNombre = materiaNombre,
                                nuevoEstado = estado,
                                ubicacion = ubicacion,
                                enlaceVirtual = enlace
                            )
                        },
                        enabled = !uiState.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(estado.etiqueta)
                    }
                }
        }
    }
}

/**
 * Vista del ALUMNO: recibe la alerta ligera y ve de inmediato la ubicación
 * física o el enlace virtual activo del profesor (Acceso Directo, 3.2).
 */
@Composable
fun PresenciaAlumnoScreen(
    usuario: Usuario,
    viewModel: PresenciaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuario.uid) {
        viewModel.inicializarComoAlumno(usuario)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text("Presencia de mis profesores", style = MaterialTheme.typography.titleLarge)

        if (uiState.presenciasDeProfesores.isEmpty()) {
            Text(
                "Ningún profesor ha actualizado su estado todavía.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(uiState.presenciasDeProfesores, key = { it.profesorUid }) { presencia ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(presencia.profesorNombre, style = MaterialTheme.typography.titleMedium)
                            Text(presencia.materiaNombre, style = MaterialTheme.typography.bodyMedium)
                            Text(presencia.estado.etiqueta, style = MaterialTheme.typography.bodyMedium)
                            val ubicacion = presencia.ubicacion.ifBlank { presencia.enlaceVirtual }
                            if (ubicacion.isNotBlank()) {
                                Text(ubicacion, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
