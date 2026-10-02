package com.example.smartvendors.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.AuthRepository
import com.example.smartvendors.domain.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()


    fun register(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.register(
                email = email,
                password = password
            )

            result
                .onSuccess {

                    val verificationResult =
                        sendEmailVerificationInternal()

                    verificationResult
                        .onSuccess {
                            _authState.value = AuthState.VerificationEmailSent(
                                "Cuenta creada. Revisa tu correo para verificarla."
                            )
                        }
                        .onFailure { exception ->
                            _authState.value = AuthState.Error(
                                exception.message
                                    ?: "Cuenta creada, pero no se pudo enviar el correo de verificación"
                            )
                        }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Error al registrarse"
                    )
                }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.checkEmailVerification()

            result
                .onSuccess { verified ->

                    if (verified) {

                        _authState.value = AuthState.Success(
                            "Correo verificado correctamente"
                        )

                    } else {

                        _authState.value = AuthState.Error(
                            "El correo todavía no ha sido verificado"
                        )
                    }
                }
                .onFailure { exception ->

                    _authState.value = AuthState.Error(
                        exception.message
                            ?: "No se pudo comprobar la verificación"
                    )
                }
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.login(
                email = email,
                password = password
            )

            result
                .onSuccess {
                    _authState.value = AuthState.Success(
                        "Inicio de sesión correcto"
                    )
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.signInWithGoogle(idToken)

            result
                .onSuccess {

                    _authState.value = AuthState.Success(
                        "Inicio de sesión con Google correcto"
                    )
                }
                .onFailure { exception ->

                    _authState.value = AuthState.Error(
                        exception.message
                            ?: "Error al iniciar sesión con Google"
                    )
                }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.sendEmailVerification()

            result
                .onSuccess {
                    _authState.value = AuthState.VerificationEmailSent(
                        "Correo de verificación enviado"
                    )
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message
                            ?: "Error al enviar el correo de verificación"
                    )
                }
        }
    }

    private suspend fun sendEmailVerificationInternal(): Result<Unit> {
        return repository.sendEmailVerification()
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val result = repository.sendPasswordReset(email)

            result
                .onSuccess {
                    _authState.value = AuthState.Success(
                        "Correo de recuperación enviado"
                    )
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message
                            ?: "Error al recuperar la contraseña"
                    )
                }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return repository.getCurrentUser() != null
    }

    fun clearState() {
        _authState.value = AuthState.Idle
    }
}