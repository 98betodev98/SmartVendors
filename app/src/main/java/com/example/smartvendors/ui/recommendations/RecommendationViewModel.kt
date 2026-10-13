
package com.example.smartvendors.ui.recommendations

import androidx.lifecycle.ViewModel
import com.example.smartvendors.data.repository.ProductRepository
import com.example.smartvendors.data.repository.RecommendationRepository
import com.example.smartvendors.domain.model.ProductRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.smartvendors.data.remote.GeminiRecommendationService
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

class RecommendationViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val recommendationRepository = RecommendationRepository()

    private val geminiService = GeminiRecommendationService()
    private val _recommendations =
        MutableStateFlow<List<ProductRecommendation>>(emptyList())
    val recommendations: StateFlow<List<ProductRecommendation>> =
        _recommendations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage



    fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                withTimeout(45_000L) {
                    val products = productRepository.getProducts()
                        .getOrElse { exception ->
                            throw exception
                        }

                    val availableProducts = products
                        .filter { it.stock > 0 }
                        .take(25)

                    if (availableProducts.isEmpty()) {
                        _recommendations.value = emptyList()
                        _errorMessage.value =
                            "No hay productos disponibles para recomendar."
                        return@withTimeout
                    }

                    val productData = availableProducts.joinToString("\n") { product ->
                        """
                    ID: ${product.id}
                    Nombre: ${product.title}
                    Categoría: ${product.category}
                    Precio: ${product.price}
                    Descuento: ${product.discountPercentage}%
                    Valoración: ${product.rating}/5
                    Stock: ${product.stock}
                    """.trimIndent()
                    }

                    val response = geminiService
                        .generateRecommendations(productData)
                        .getOrElse { exception ->
                            throw exception
                        }

                    val recommendations = response
                        .lineSequence()
                        .mapNotNull { line ->
                            val match = Regex(
                                """ID:\s*(\d+)\s*\|\s*MOTIVO:\s*(.+)""",
                                RegexOption.IGNORE_CASE
                            ).find(line) ?: return@mapNotNull null

                            val id = match.groupValues[1].toIntOrNull()
                                ?: return@mapNotNull null
                            val reason = match.groupValues[2].trim()

                            val product = availableProducts
                                .firstOrNull { it.id == id }
                                ?: return@mapNotNull null

                            if (reason.isBlank()) return@mapNotNull null

                            product to reason
                        }
                        .distinctBy { it.first.id }
                        .take(5)
                        .map { (product, reason) ->
                            val ratingScore =
                                (product.rating / 5.0).coerceIn(0.0, 1.0) * 50.0
                            val discountScore =
                                (product.discountPercentage / 100.0)
                                    .coerceIn(0.0, 1.0) * 30.0
                            val stockScore =
                                if (product.stock > 10) 20.0 else 10.0

                            ProductRecommendation(
                                product = product,
                                score = ratingScore + discountScore + stockScore,
                                reason = reason
                            )
                        }
                        .sortedByDescending { it.score }
                        .toList()

                    _recommendations.value = recommendations

                    if (recommendations.isEmpty()) {
                        _errorMessage.value =
                            "La IA no devolvió recomendaciones válidas. Intenta nuevamente."
                    }
                }
            } catch (exception: TimeoutCancellationException) {
                _errorMessage.value =
                    "La solicitud tardó más de 45 segundos. Intenta nuevamente."
            } catch (exception: Exception) {
                _errorMessage.value =
                    exception.message ?: "Ocurrió un error al generar recomendaciones."
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun clearError() {
        _errorMessage.value = null
    }
}
