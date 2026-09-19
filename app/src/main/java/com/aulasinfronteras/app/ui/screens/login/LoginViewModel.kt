package com.aulasinfronteras.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasinfronteras.app.data.model.RolUsuario
import com.aulasinfronteras.app.data.model.Usuario
import com.aulasinfronteras.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val usuario: Usuario? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, error = null)
    }

    fun onEmailChange(valor: String) {
        _uiState.value = _uiState.value.copy(email = valor, error = null)
    }

    fun onPasswordChange(valor: String) {
        _uiState.value = _uiState.value.copy(password = valor, error = null)
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.email.isBlank() || estado.password.isBlank()) {
            _uiState.value = estado.copy(error = "Ingresa tu correo y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            val resultado = authRepository.iniciarSesion(estado.email.trim(), estado.password)
            resultado.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(cargando = false, usuario = usuario)
            }.onFailure { excepcion ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = excepcion.localizedMessage ?: "No se pudo iniciar sesión"
                )
            }
        }
    }

    /** Registro rápido, usado principalmente en pruebas / alta de administrador inicial. */
    fun registrar(rol: RolUsuario) {
        val estado = _uiState.value
        if (estado.email.isBlank() || estado.password.isBlank() || estado.nombre.isBlank()) {
            _uiState.value = estado.copy(error = "Completa nombre, correo y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            val resultado = authRepository.registrarUsuario(
                estado.email.trim(), estado.password, estado.nombre.trim(), rol
            )
            resultado.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(cargando = false, usuario = usuario)
            }.onFailure { excepcion ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = excepcion.localizedMessage ?: "No se pudo registrar el usuario"
                )
            }
        }
    }
}
