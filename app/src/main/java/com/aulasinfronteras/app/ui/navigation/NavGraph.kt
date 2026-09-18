package com.aulasinfronteras.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aulasinfronteras.app.data.model.Usuario
import com.aulasinfronteras.app.ui.screens.avisos.AvisosScreen
import com.aulasinfronteras.app.ui.screens.avisos.RedactarAvisoScreen
import com.aulasinfronteras.app.ui.screens.calendario.CalendarioScreen
import com.aulasinfronteras.app.ui.screens.calendario.CrearEventoScreen
import com.aulasinfronteras.app.ui.screens.login.LoginScreen
import com.aulasinfronteras.app.ui.screens.perfil.PerfilScreen
import com.aulasinfronteras.app.ui.screens.presencia.PresenciaAlumnoScreen
import com.aulasinfronteras.app.ui.screens.presencia.PresenciaProfesorScreen

private object Rutas {
    const val LOGIN = "login"
    const val CALENDARIO = "calendario"
    const val CREAR_EVENTO = "crear_evento"
    const val PRESENCIA = "presencia"
    const val AVISOS = "avisos"
    const val REDACTAR_AVISO = "redactar_aviso"
    const val PERFIL = "perfil"
}

/**
 * Grafo de navegación único para los 3 roles. Cada pantalla decide
 * internamente qué mostrar/habilitar según el rol del usuario logueado
 * (UI Adaptativa, sección 2 del documento de especificación).
 */
@Composable
fun AulasNavGraph() {
    val navController = rememberNavController()
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    if (usuarioActual == null) {
        LoginScreen(onLoginExitoso = { usuarioActual = it })
        return
    }

    val usuario = usuarioActual!!

    Scaffold(
        bottomBar = { BarraInferior(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.CALENDARIO,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.CALENDARIO) {
                CalendarioScreen(
                    usuario = usuario,
                    onCrearEvento = { navController.navigate(Rutas.CREAR_EVENTO) }
                )
            }
            composable(Rutas.CREAR_EVENTO) {
                CrearEventoScreen(
                    creadoPorUid = usuario.uid,
                    onEventoCreado = { navController.popBackStack() }
                )
            }
            composable(Rutas.PRESENCIA) {
                if (usuario.isTeacher) {
                    val materiaId = usuario.materiasImpartidas.firstOrNull().orEmpty()
                    PresenciaProfesorScreen(
                        usuario = usuario,
                        materiaId = materiaId,
                        materiaNombre = materiaId
                    )
                } else {
                    PresenciaAlumnoScreen(usuario = usuario)
                }
            }
            composable(Rutas.AVISOS) {
                AvisosScreen(
                    usuario = usuario,
                    onRedactarAviso = { navController.navigate(Rutas.REDACTAR_AVISO) }
                )
            }
            composable(Rutas.REDACTAR_AVISO) {
                RedactarAvisoScreen(
                    usuario = usuario,
                    onAvisoEnviado = { navController.popBackStack() }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    usuario = usuario,
                    onCerrarSesion = {
                        usuarioActual = null
                        navController.navigate(Rutas.CALENDARIO) { popUpTo(0) }
                    }
                )
            }
        }
    }
}

@Composable
private fun BarraInferior(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = rutaActual == Rutas.CALENDARIO,
            onClick = { navController.navigate(Rutas.CALENDARIO) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario") },
            label = { Text("Calendario") }
        )
        NavigationBarItem(
            selected = rutaActual == Rutas.PRESENCIA,
            onClick = { navController.navigate(Rutas.PRESENCIA) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Person, contentDescription = "Presencia") },
            label = { Text("Presencia") }
        )
        NavigationBarItem(
            selected = rutaActual == Rutas.AVISOS,
            onClick = { navController.navigate(Rutas.AVISOS) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos") },
            label = { Text("Avisos") }
        )
        NavigationBarItem(
            selected = rutaActual == Rutas.PERFIL,
            onClick = { navController.navigate(Rutas.PERFIL) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}
