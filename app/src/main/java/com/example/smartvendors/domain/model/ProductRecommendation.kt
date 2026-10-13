
package com.example.smartvendors.domain.model

data class ProductRecommendation(
    val product: Product,
    val score: Double,
    val reason: String
)