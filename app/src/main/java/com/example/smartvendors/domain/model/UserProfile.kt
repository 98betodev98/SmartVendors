package com.example.smartvendors.domain.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val fotoPerfil: String = "",
    val recibirOfertas: Boolean = true,
    val recibirCapacitaciones: Boolean = true,
    val recibirNotificaciones: Boolean = true
)