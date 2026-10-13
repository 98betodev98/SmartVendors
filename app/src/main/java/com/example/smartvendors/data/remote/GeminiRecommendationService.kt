
package com.example.smartvendors.data.remote

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRecommendationService {

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-3.5-flash-lite"
    )

    suspend fun generateRecommendations(
        productData: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
            Eres un asistente de ventas por catálogo.
            Recomienda los 3 productos más interesantes del catálogo.
            Prioriza buena valoración, descuento y stock disponible.
        
            Devuelve exactamente 3 líneas con este formato:
            ID: número | MOTIVO: explicación de máximo 12 palabras
        
            Usa únicamente los ID proporcionados.
            No inventes datos. No agregues introducciones ni conclusiones.
        
            CATÁLOGO:
            $productData
        """.trimIndent()

            val response = model.generateContent(prompt)

            response.text
                ?.takeIf { it.isNotBlank() }
                ?: error("Gemini no devolvió recomendaciones.")
        }
    }
}
