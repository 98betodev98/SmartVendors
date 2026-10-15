
package com.example.smartvendors.data.remote

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAssistantService {

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-3.5-flash-lite"
    )

    suspend fun ask(
        question: String,
        productData: String
    ): String = withContext(Dispatchers.IO) {

        val prompt = """
            Eres el asistente virtual de ventas de SmartVendors.
            Responde siempre en español, de forma amable y concisa.
        
            REGLAS:
            - Responde directamente a la pregunta del usuario.
            - No enumeres todos los productos si no te lo solicitan.
            - Si preguntan por descuentos, menciona como máximo 5 productos.
            - Si preguntan qué vender, recomienda como máximo 3 productos.
            - Incluye el precio y descuento únicamente cuando sean relevantes.
            - Usa solo los datos del catálogo proporcionado.
            - No inventes precios, descuentos ni stock.
            - Si no tienes información suficiente, indícalo.
            - Evita introducciones largas y respuestas innecesariamente extensas.
        
            CATÁLOGO:
            $productData
        
            PREGUNTA:
            $question
        """.trimIndent()

        val response = model.generateContent(prompt)

        response.text
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "El asistente no generó una respuesta."
            )
    }
}
