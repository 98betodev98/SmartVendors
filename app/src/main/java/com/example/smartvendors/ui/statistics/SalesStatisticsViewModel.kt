
package com.example.smartvendors.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.OrderRepository
import com.example.smartvendors.domain.model.Order
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlySalesPoint(
    val label: String,
    val amount: Double
)

data class ProductSalesPoint(
    val productName: String,
    val units: Int,
    val revenue: Double
)

data class SalesStatistics(
    val totalOrders: Int = 0,
    val totalAmount: Double = 0.0,
    val monthlyAmount: Double = 0.0,
    val totalUnits: Int = 0,
    val pendingOrders: Int = 0,
    val orders: List<Order> = emptyList(),

    // Datos para los gráficos
    val monthlySales: List<MonthlySalesPoint> = emptyList(),
    val topProductsByUnits: List<ProductSalesPoint> = emptyList(),
    val topProductsByRevenue: List<ProductSalesPoint> = emptyList()
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

                        // 1. Ventas de los últimos seis meses

                        val monthKeyFormat =
                            SimpleDateFormat("yyyy-MM", Locale("es", "PE"))

                        val monthLabelFormat =
                            SimpleDateFormat("MMM", Locale("es", "PE"))

                        val monthlyAmounts = mutableMapOf<String, Double>()

                        orders.forEach { order ->
                            val key = monthKeyFormat.format(Date(order.fecha))

                            monthlyAmounts[key] =
                                (monthlyAmounts[key] ?: 0.0) + order.total
                        }

                        val monthlySales = mutableListOf<MonthlySalesPoint>()

                        val monthCalendar = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            add(Calendar.MONTH, -5)
                        }

                        repeat(6) {

                            val date = monthCalendar.time
                            val key = monthKeyFormat.format(date)

                            val label = monthLabelFormat.format(date)
                                .replaceFirstChar { it.uppercase() }

                            monthlySales.add(
                                MonthlySalesPoint(
                                    label = label,
                                    amount = monthlyAmounts[key] ?: 0.0
                                )
                            )

                            monthCalendar.add(Calendar.MONTH, 1)
                        }

                        // 2. Productos más vendidos e ingresos por producto

                        val productTotals =
                            mutableMapOf<String, ProductSalesPoint>()

                        orders.forEach { order ->

                            order.items.forEach { item ->

                                val key = if (item.productId > 0) {
                                    "id:${item.productId}"
                                } else {
                                    "title:${item.title.trim().lowercase()}"
                                }

                                val quantity = item.quantity.coerceAtLeast(0)

                                val current = productTotals[key]

                                productTotals[key] = ProductSalesPoint(
                                    productName = current?.productName
                                        ?: item.title.ifBlank { "Producto sin nombre" },

                                    units = (current?.units ?: 0) + quantity,

                                    revenue = (current?.revenue ?: 0.0) +
                                            (item.price * quantity)
                                )
                            }
                        }

                        val topProductsByUnits = productTotals.values
                            .sortedByDescending { it.units }
                            .take(5)

                        val topProductsByRevenue = productTotals.values
                            .sortedByDescending { it.revenue }
                            .take(5)

                        // 3. Resumen general de ventas

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
                                it.estado.equals("Pendiente", ignoreCase = true)
                            },

                            orders = orders,

                            monthlySales = monthlySales,

                            topProductsByUnits = topProductsByUnits,

                            topProductsByRevenue = topProductsByRevenue
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
