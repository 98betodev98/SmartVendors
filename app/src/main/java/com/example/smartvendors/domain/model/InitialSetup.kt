package com.example.smartvendors.domain.model

data class InitialSetup(
    val uid: String = "",
    val nombreNegocio: String = "",
    val categoriaVenta: String = "",
    val configuracionCompletada: Boolean = false
)