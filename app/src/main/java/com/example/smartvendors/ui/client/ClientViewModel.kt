package com.example.smartvendors.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.ClientRepository
import com.example.smartvendors.domain.model.Client
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientViewModel : ViewModel() {

    private val repository = ClientRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun loadClients() {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            _errorMessage.value = "No hay un usuario autenticado"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getClients(uid)

            result
                .onSuccess { clientList ->
                    _clients.value = clientList
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al cargar los clientes"
                }

            _isLoading.value = false
        }
    }

    fun addClient(client: Client) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            _errorMessage.value = "No hay un usuario autenticado"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = repository.addClient(uid, client)

            result
                .onSuccess {
                    _successMessage.value = "Cliente registrado correctamente"
                    loadClients()
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al registrar el cliente"
                }

            _isLoading.value = false
        }
    }

    fun updateClient(client: Client) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            _errorMessage.value = "No hay un usuario autenticado"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = repository.updateClient(uid, client)

            result
                .onSuccess {
                    _successMessage.value = "Cliente actualizado correctamente"
                    loadClients()
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al actualizar el cliente"
                }

            _isLoading.value = false
        }
    }

    fun deleteClient(clientId: String) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            _errorMessage.value = "No hay un usuario autenticado"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = repository.deleteClient(
                uid = uid,
                clientId = clientId
            )

            result
                .onSuccess {
                    _successMessage.value = "Cliente eliminado correctamente"
                    loadClients()
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al eliminar el cliente"
                }

            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}