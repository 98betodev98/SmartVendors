
package com.example.smartvendors.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.remote.GeminiAssistantService
import com.example.smartvendors.data.repository.ProductRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class AssistantViewModel : ViewModel() {

    private val productRepository = ProductRepository()
    private val geminiService = GeminiAssistantService()

    private val _messages: MutableStateFlow<List<ChatMessage>> =
        MutableStateFlow(
            listOf(
                ChatMessage(
                    text = "¡Hola! Soy tu asistente virtual de ventas. ¿En qué puedo ayudarte?",
                    isUser = false
                )
            )
        )

    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(question: String) {
        val cleanQuestion = question.trim()

        if (cleanQuestion.isBlank() || _isLoading.value) return

        _messages.value = _messages.value + ChatMessage(
            text = cleanQuestion,
            isUser = true
        )

        _isLoading.value = true

        viewModelScope.launch {
            try {
                withTimeout(45_000L) {
                    val products = productRepository.getProducts()
                        .getOrElse { throw it }

                    val productData = products
                        .take(15)
                        .joinToString("\n") { product ->
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

                    val answer = geminiService.ask(
                        question = cleanQuestion,
                        productData = productData
                    )

                    _messages.value = _messages.value + ChatMessage(
                        text = answer,
                        isUser = false
                    )
                }
            } catch (exception: TimeoutCancellationException) {
                _messages.value = _messages.value + ChatMessage(
                    text = "La consulta tardó demasiado. Intenta nuevamente.",
                    isUser = false
                )
            } catch (exception: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    text = "No pude procesar tu consulta. Verifica tu conexión e inténtalo nuevamente.",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
