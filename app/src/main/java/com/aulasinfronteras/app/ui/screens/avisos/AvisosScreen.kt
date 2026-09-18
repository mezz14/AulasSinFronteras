package com.aulasinfronteras.app.ui.screens.avisos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aulasinfronteras.app.data.model.Aviso
import com.aulasinfronteras.app.data.model.Usuario

/**
 * Bandeja de avisos, visible para los 3 roles. El FAB para redactar un
 * aviso nuevo solo aparece si usuario.isAdmin || usuario.isTeacher
 * (el Alumno tiene acceso de solo lectura, sección 2).
 */
@Composable
fun AvisosScreen(
    usuario: Usuario,
    onRedactarAviso: () -> Unit,
    viewModel: AvisosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuario.uid) {
        viewModel.inicializar(usuario)
    }

    Scaffold(
        floatingActionButton = {
            if (usuario.isAdmin || usuario.isTeacher) {
                FloatingActionButton(onClick = onRedactarAviso) {
                    Icon(Icons.Default.Add, contentDescription = "Redactar aviso")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Avisos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            ListaDeAvisos(uiState.avisos)
        }
    }
}

@Composable
private fun ListaDeAvisos(avisos: List<Aviso>) {
    if (avisos.isEmpty()) {
        Text("No hay avisos por ahora.", modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(avisos, key = { it.id }) { aviso ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(aviso.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(aviso.contenido, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${aviso.canal.name} · ${aviso.creadoPorNombre}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
