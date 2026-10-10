
package com.example.smartvendors.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.OrderRepository
import com.example.smartvendors.domain.model.Order
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun confirmOrder(
        items: List<com.example.smartvendors.domain.model.CartItem>,
        total: Double,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return

        val uid = auth.currentUser?.uid

        if (uid == null) {
            _errorMessage.value = "No hay un usuario autenticado"
            return
        }

        if (items.isEmpty()) {
            _errorMessage.value = "El carrito está vacío"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val order = Order(
                uid = uid,
                items = items,
                total = total,
                fecha = System.currentTimeMillis(),
                estado = "Pendiente"
            )

            repository.saveOrder(order)
                .onSuccess {
                    _successMessage.value = "Pedido registrado correctamente"
                    onSuccess()
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al registrar el pedido"
                }

            _isSaving.value = false
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}
