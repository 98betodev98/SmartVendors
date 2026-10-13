
package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.Product
import com.example.smartvendors.domain.model.ProductRecommendation

class RecommendationRepository {

    fun getRecommendations(
        products: List<Product>
    ): List<ProductRecommendation> {

        return products
            .filter { product -> product.stock > 0 }
            .map { product ->

                val ratingScore =
                    (product.rating / 5.0).coerceIn(0.0, 1.0) * 50.0

                val discountScore =
                    (product.discountPercentage / 100.0)
                        .coerceIn(0.0, 1.0) * 30.0

                val stockScore =
                    if (product.stock > 10) 20.0 else 10.0

                val score = ratingScore + discountScore + stockScore

                val reason = when {
                    product.rating >= 4.5 &&
                            product.discountPercentage >= 10 ->
                        "Excelente valoración y buen descuento"

                    product.rating >= 4.5 ->
                        "Excelente valoración de los compradores"

                    product.discountPercentage >= 15 ->
                        "Destaca por su descuento"

                    else ->
                        "Producto disponible para recomendar"
                }

                ProductRecommendation(
                    product = product,
                    score = score,
                    reason = reason
                )
            }
            .sortedByDescending { recommendation -> recommendation.score }
    }
}
