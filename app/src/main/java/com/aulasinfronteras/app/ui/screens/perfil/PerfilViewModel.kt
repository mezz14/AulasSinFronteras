package com.aulasinfronteras.app.ui.screens.perfil

import androidx.lifecycle.ViewModel
import com.aulasinfronteras.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun cerrarSesion() = authRepository.cerrarSesion()
}
