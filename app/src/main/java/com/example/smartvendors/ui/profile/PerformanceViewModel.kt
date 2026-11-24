package com.example.smartvendors.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerformanceViewModel : ViewModel() {

    private val repository = PerformanceRepository()

    private val _totalPedidos = MutableStateFlow(0)
    val totalPedidos: StateFlow<Int> = _totalPedidos.asStateFlow()

    private val _ventasTotales = MutableStateFlow(0.0)
    val ventasTotales: StateFlow<Double> = _ventasTotales.asStateFlow()

    private val _pedidosPendientes = MutableStateFlow(0)
    val pedidosPendientes: StateFlow<Int> = _pedidosPendientes.asStateFlow()

    private val _unidadesVendidas = MutableStateFlow(0)
    val unidadesVendidas: StateFlow<Int> = _unidadesVendidas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPerformance() {
        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            val result = repository.getPerformanceData()

            result
                .onSuccess { datos ->

                    _totalPedidos.value =
                        datos["totalPedidos"] as Int

                    _ventasTotales.value =
                        datos["ventasTotales"] as Double

                    _pedidosPendientes.value =
                        datos["pedidosPendientes"] as Int

                    _unidadesVendidas.value =
                        datos["unidadesVendidas"] as Int
                }
                .onFailure { exception ->
                    _error.value = exception.message
                        ?: "No se pudieron cargar los datos de rendimiento"
                }

            _isLoading.value = false
        }
    }

    fun refresh() {
        loadPerformance()
    }
}
