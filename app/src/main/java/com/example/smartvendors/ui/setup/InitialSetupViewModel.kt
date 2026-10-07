package com.example.smartvendors.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.InitialSetupRepository
import com.example.smartvendors.domain.model.InitialSetup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InitialSetupState {

    data object Idle : InitialSetupState

    data object Loading : InitialSetupState

    data class Success(
        val message: String
    ) : InitialSetupState

    data class Error(
        val message: String
    ) : InitialSetupState
}

class InitialSetupViewModel : ViewModel() {

    private val repository =
        InitialSetupRepository()

    private val auth =
        FirebaseAuth.getInstance()

    private val _nombreNegocio =
        MutableStateFlow("")

    val nombreNegocio: StateFlow<String> =
        _nombreNegocio.asStateFlow()

    private val _categoriaVenta =
        MutableStateFlow("")

    val categoriaVenta: StateFlow<String> =
        _categoriaVenta.asStateFlow()

    private val _setupState =
        MutableStateFlow<InitialSetupState>(
            InitialSetupState.Idle
        )

    val setupState:
            StateFlow<InitialSetupState> =
        _setupState.asStateFlow()

    fun setNombreNegocio(
        value: String
    ) {
        _nombreNegocio.value = value
    }

    fun setCategoriaVenta(
        value: String
    ) {
        _categoriaVenta.value = value
    }

    fun loadSetup() {

        val user = auth.currentUser

        if (user == null) {

            _setupState.value =
                InitialSetupState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        viewModelScope.launch {

            _setupState.value =
                InitialSetupState.Loading

            val result =
                repository.getSetup(user.uid)

            result
                .onSuccess { setup ->

                    if (setup != null) {

                        _nombreNegocio.value =
                            setup.nombreNegocio

                        _categoriaVenta.value =
                            setup.categoriaVenta
                    }

                    _setupState.value =
                        InitialSetupState.Idle
                }
                .onFailure { exception ->

                    _setupState.value =
                        InitialSetupState.Error(
                            exception.message
                                ?: "No se pudo cargar la configuración"
                        )
                }
        }
    }

    fun saveSetup() {

        val user = auth.currentUser

        if (user == null) {

            _setupState.value =
                InitialSetupState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        if (_nombreNegocio.value.isBlank()) {

            _setupState.value =
                InitialSetupState.Error(
                    "Ingresa el nombre de tu negocio"
                )

            return
        }

        if (_categoriaVenta.value.isBlank()) {

            _setupState.value =
                InitialSetupState.Error(
                    "Selecciona una categoría de venta"
                )

            return
        }

        viewModelScope.launch {

            _setupState.value =
                InitialSetupState.Loading

            val setup =
                InitialSetup(
                    uid = user.uid,
                    nombreNegocio =
                        _nombreNegocio.value.trim(),
                    categoriaVenta =
                        _categoriaVenta.value,
                    configuracionCompletada = true
                )

            val result =
                repository.saveSetup(setup)

            result
                .onSuccess {

                    _setupState.value =
                        InitialSetupState.Success(
                            "Configuración guardada correctamente"
                        )
                }
                .onFailure { exception ->

                    _setupState.value =
                        InitialSetupState.Error(
                            exception.message
                                ?: "No se pudo guardar la configuración"
                        )
                }
        }
    }

    fun checkSetupCompleted(
        onResult: (Boolean) -> Unit
    ) {

        val user = auth.currentUser

        if (user == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {

            val result =
                repository.isSetupCompleted(
                    user.uid
                )

            result
                .onSuccess { completed ->
                    onResult(completed)
                }
                .onFailure {
                    onResult(false)
                }
        }
    }

    fun clearState() {

        _setupState.value =
            InitialSetupState.Idle
    }
}