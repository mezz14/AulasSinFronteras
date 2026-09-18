package com.aulasinfronteras.app.ui.screens.perfil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aulasinfronteras.app.data.model.Usuario

@Composable
fun PerfilScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.titleLarge)
        Text(usuario.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        Text(usuario.email, style = MaterialTheme.typography.bodyMedium)
        Text("Rol: ${usuario.rol.name}", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                viewModel.cerrarSesion()
                onCerrarSesion()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}
