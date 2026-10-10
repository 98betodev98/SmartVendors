package com.example.smartvendors.domain.model

data class Order(
    val id: String = "",
    val uid: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "Pendiente"
)