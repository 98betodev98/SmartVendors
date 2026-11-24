package com.example.smartvendors.domain.model

data class AdminConsultant(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val fotoPerfil: String = "",
    val rol: String = "USER",
    val activo: Boolean = true
)