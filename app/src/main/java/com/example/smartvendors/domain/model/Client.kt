package com.example.smartvendors.domain.model

data class Client(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val documento: String = "",
    val telefono: String = "",
    val correo: String = "",
    val direccion: String = ""
)