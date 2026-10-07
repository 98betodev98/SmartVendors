package com.example.smartvendors.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PreferencesState {

    data object Idle : PreferencesState

    data object Loading : PreferencesState

    data class Success(
        val message: String
    ) : PreferencesState

    data class Error(
        val message: String
    ) : PreferencesState
}

class PreferencesViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val auth = FirebaseAuth.getInstance()

    private val _recibirOfertas =
        MutableStateFlow(true)

    val recibirOfertas: StateFlow<Boolean> =
        _recibirOfertas.asStateFlow()

    private val _recibirCapacitaciones =
        MutableStateFlow(true)

    val recibirCapacitaciones: StateFlow<Boolean> =
        _recibirCapacitaciones.asStateFlow()

    private val _recibirNotificaciones =
        MutableStateFlow(true)

    val recibirNotificaciones: StateFlow<Boolean> =
        _recibirNotificaciones.asStateFlow()

    private val _preferencesState =
        MutableStateFlow<PreferencesState>(
            PreferencesState.Idle
        )

    val preferencesState:
            StateFlow<PreferencesState> =
        _preferencesState.asStateFlow()

    fun loadPreferences() {

        val user = auth.currentUser

        if (user == null) {

            _preferencesState.value =
                PreferencesState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        viewModelScope.launch {

            _preferencesState.value =
                PreferencesState.Loading

            val result =
                repository.getProfile(user.uid)

            result
                .onSuccess { profile ->

                    _recibirOfertas.value =
                        profile.recibirOfertas

                    _recibirCapacitaciones.value =
                        profile.recibirCapacitaciones

                    _recibirNotificaciones.value =
                        profile.recibirNotificaciones

                    _preferencesState.value =
                        PreferencesState.Idle
                }
                .onFailure { exception ->

                    _preferencesState.value =
                        PreferencesState.Error(
                            exception.message
                                ?: "No se pudieron cargar las preferencias"
                        )
                }
        }
    }

    fun setRecibirOfertas(
        value: Boolean
    ) {

        _recibirOfertas.value = value
    }

    fun setRecibirCapacitaciones(
        value: Boolean
    ) {

        _recibirCapacitaciones.value = value
    }

    fun setRecibirNotificaciones(
        value: Boolean
    ) {

        _recibirNotificaciones.value = value
    }

    fun savePreferences() {

        val user = auth.currentUser

        if (user == null) {

            _preferencesState.value =
                PreferencesState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        viewModelScope.launch {

            _preferencesState.value =
                PreferencesState.Loading

            val currentProfileResult =
                repository.getProfile(user.uid)

            currentProfileResult
                .onSuccess { currentProfile ->

                    val updatedProfile =
                        currentProfile.copy(

                            recibirOfertas =
                                _recibirOfertas.value,

                            recibirCapacitaciones =
                                _recibirCapacitaciones.value,

                            recibirNotificaciones =
                                _recibirNotificaciones.value
                        )

                    val saveResult =
                        repository.saveProfile(
                            updatedProfile
                        )

                    saveResult
                        .onSuccess {

                            _preferencesState.value =
                                PreferencesState.Success(
                                    "Preferencias guardadas correctamente"
                                )
                        }
                        .onFailure { exception ->

                            _preferencesState.value =
                                PreferencesState.Error(
                                    exception.message
                                        ?: "No se pudieron guardar las preferencias"
                                )
                        }
                }
                .onFailure { exception ->

                    _preferencesState.value =
                        PreferencesState.Error(
                            exception.message
                                ?: "No se pudo obtener el perfil"
                        )
                }
        }
    }

    fun clearState() {

        _preferencesState.value =
            PreferencesState.Idle
    }
}