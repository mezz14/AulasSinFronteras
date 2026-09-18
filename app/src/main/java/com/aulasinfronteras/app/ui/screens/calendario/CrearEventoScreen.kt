package com.aulasinfronteras.app.ui.screens.calendario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aulasinfronteras.app.data.model.Evento
import com.google.firebase.Timestamp

/** Formulario simple para que el Administrador cree un evento/calendario general. */
@Composable
fun CrearEventoScreen(
    creadoPorUid: String,
    onEventoCreado: () -> Unit,
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var materiaId by remember { mutableStateOf("") }
    var materiaNombre by remember { mutableStateOf("") }
    var aula by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text("Nuevo evento de campus", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it },
            label = { Text("Título") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = materiaId, onValueChange = { materiaId = it },
            label = { Text("ID de materia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = materiaNombre, onValueChange = { materiaNombre = it },
            label = { Text("Nombre de materia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = aula, onValueChange = { aula = it },
            label = { Text("Aula / enlace virtual") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Button(
            onClick = {
                val evento = Evento(
                    titulo = titulo,
                    materiaId = materiaId,
                    materiaNombre = materiaNombre,
                    aula = aula,
                    fechaInicio = Timestamp.now(),
                    fechaFin = Timestamp.now(),
                    creadoPorUid = creadoPorUid
                )
                viewModel.crearEvento(evento) { resultado ->
                    if (resultado.isSuccess) onEventoCreado()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Text("Guardar evento")
        }
    }
}
