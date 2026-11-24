package com.example.smartvendors.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.AdminConsultantRepository
import com.example.smartvendors.domain.model.AdminConsultant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminConsultantViewModel : ViewModel() {

    private val repository = AdminConsultantRepository()

    private val _consultants = MutableStateFlow<List<AdminConsultant>>(emptyList())
    val consultants: StateFlow<List<AdminConsultant>> = _consultants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadConsultants() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getConsultants()

            result
                .onSuccess { list ->
                    _consultants.value = list
                }
                .onFailure { exception ->
                    _error.value = exception.message
                        ?: "No se pudieron cargar las consultoras"
                }

            _isLoading.value = false
        }
    }

    fun refresh() {
        loadConsultants()
    }

    fun updateConsultantStatus(
        uid: String,
        activo: Boolean
    ) {
        viewModelScope.launch {

            _error.value = null

            val result = repository.updateConsultantStatus(
                uid = uid,
                activo = activo
            )

            result
                .onSuccess {
                    loadConsultants()
                }
                .onFailure { exception ->
                    _error.value = exception.message
                        ?: "No se pudo actualizar el estado de la consultora"
                }
        }
    }
}