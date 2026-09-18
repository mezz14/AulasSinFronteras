package com.aulasinfronteras.app.ui.screens.avisos

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
import com.aulasinfronteras.app.data.model.CanalAviso
import com.aulasinfronteras.app.data.model.Usuario

@Composable
fun RedactarAvisoScreen(
    usuario: Usuario,
    onAvisoEnviado: () -> Unit,
    viewModel: AvisosViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    // El Administrador redacta avisos institucionales (materiaId vacío);
    // el Profesor debe indicar la materia a la que pertenece el aviso.
    var materiaId by remember { mutableStateOf(if (usuario.isAdmin) "" else usuario.materiasImpartidas.firstOrNull().orEmpty()) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text(
            if (usuario.isAdmin) "Nuevo aviso institucional" else "Nuevo aviso de materia",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it },
            label = { Text("Título") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = contenido, onValueChange = { contenido = it },
            label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        if (!usuario.isAdmin) {
            OutlinedTextField(
                value = materiaId, onValueChange = { materiaId = it },
                label = { Text("ID de materia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                val canal = if (usuario.isAdmin) CanalAviso.URGENTE else CanalAviso.CAMBIOS_AULA
                viewModel.crearAviso(titulo, contenido, canal, materiaId, usuario) { resultado ->
                    if (resultado.isSuccess) onAvisoEnviado()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Text("Enviar aviso")
        }
    }
}
