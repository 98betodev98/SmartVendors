
package com.example.smartvendors.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.OrderRepository
import com.example.smartvendors.domain.model.Order
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class SalesStatistics(
    val totalOrders: Int = 0,
    val totalAmount: Double = 0.0,
    val monthlyAmount: Double = 0.0,
    val totalUnits: Int = 0,
    val pendingOrders: Int = 0,
    val orders: List<Order> = emptyList()
)

class SalesStatisticsViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _statistics = MutableStateFlow(SalesStatistics())
    val statistics: StateFlow<SalesStatistics> = _statistics

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadStatistics() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid.isNullOrBlank()) {
            _error.value = "No hay una sesión de usuario activa."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.getOrdersByUser(uid)
                    .onSuccess { orders ->
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val monthStart = calendar.timeInMillis

                        _statistics.value = SalesStatistics(
                            totalOrders = orders.size,
                            totalAmount = orders.sumOf { it.total },
                            monthlyAmount = orders
                                .filter { it.fecha >= monthStart }
                                .sumOf { it.total },
                            totalUnits = orders.sumOf { order ->
                                order.items.sumOf { item ->
                                    item.quantity.coerceAtLeast(0)
                                }
                            },
                            pendingOrders = orders.count {
                                it.estado.equals(
                                    "Pendiente",
                                    ignoreCase = true
                                )
                            },
                            orders = orders
                        )
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                            ?: "No se pudieron cargar las estadísticas."
                    }
            } catch (e: Exception) {
                _error.value = e.message
                    ?: "Ocurrió un error al consultar las estadísticas."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
