package com.example.smartvendors.domain.model

data class CartItem(
    val productId: Int = 0,
    val title: String = "",
    val price: Double = 0.0,
    val thumbnail: String = "",
    val quantity: Int = 1
)