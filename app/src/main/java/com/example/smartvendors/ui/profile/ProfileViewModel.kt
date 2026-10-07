package com.example.smartvendors.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.ProfileRepository
import com.example.smartvendors.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface ProfileState {

    data object Idle : ProfileState

    data object Loading : ProfileState

    data class Success(
        val message: String
    ) : ProfileState

    data class Error(
        val message: String
    ) : ProfileState
}

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val auth = FirebaseAuth.getInstance()

    private val _profile = MutableStateFlow(UserProfile())

    val profile: StateFlow<UserProfile> =
        _profile.asStateFlow()

    private val _profileState =
        MutableStateFlow<ProfileState>(
            ProfileState.Idle
        )

    val profileState: StateFlow<ProfileState> =
        _profileState.asStateFlow()

    fun loadProfile() {

        val user = auth.currentUser

        if (user == null) {

            _profileState.value =
                ProfileState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        viewModelScope.launch {

            _profileState.value =
                ProfileState.Loading

            val result =
                repository.getProfile(user.uid)

            result
                .onSuccess { profile ->

                    _profile.value = profile.copy(
                        uid = user.uid,
                        email = user.email ?: profile.email
                    )

                    _profileState.value =
                        ProfileState.Idle
                }
                .onFailure { exception ->

                    _profileState.value =
                        ProfileState.Error(
                            exception.message
                                ?: "No se pudo cargar el perfil"
                        )
                }
        }
    }

    fun updateProfile(
        nombre: String,
        apellido: String,
        fotoPerfil: String
    ) {

        val user = auth.currentUser

        if (user == null) {

            _profileState.value =
                ProfileState.Error(
                    "No hay un usuario autenticado"
                )

            return
        }

        viewModelScope.launch {

            _profileState.value =
                ProfileState.Loading

            val currentProfileResult =
                repository.getProfile(user.uid)

            currentProfileResult
                .onSuccess { currentProfile ->

                    val updatedProfile =
                        currentProfile.copy(
                            nombre = nombre,
                            apellido = apellido,
                            fotoPerfil = fotoPerfil
                        )

                    val saveResult =
                        repository.saveProfile(
                            updatedProfile
                        )

                    saveResult
                        .onSuccess {

                            _profile.value =
                                updatedProfile

                            _profileState.value =
                                ProfileState.Success(
                                    "Perfil actualizado correctamente"
                                )
                        }
                        .onFailure { exception ->

                            _profileState.value =
                                ProfileState.Error(
                                    exception.message
                                        ?: "No se pudo actualizar el perfil"
                                )
                        }
                }
                .onFailure { exception ->

                    _profileState.value =
                        ProfileState.Error(
                            exception.message
                                ?: "No se pudo obtener el perfil"
                        )
                }
        }
    }

    fun clearState() {

        _profileState.value =
            ProfileState.Idle
    }
}