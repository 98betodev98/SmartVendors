package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.AdminConsultant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminConsultantRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getConsultants(): Result<List<AdminConsultant>> {
        return try {

            val snapshot = firestore.collection("users")
                .whereEqualTo("rol", "USER")
                .get()
                .await()

            val consultants = snapshot.documents.map { document ->

                AdminConsultant(
                    uid = document.id,
                    nombre = document.getString("nombre") ?: "",
                    apellido = document.getString("apellido") ?: "",
                    email = document.getString("email") ?: "",
                    fotoPerfil = document.getString("fotoPerfil") ?: "",
                    rol = document.getString("rol") ?: "USER",
                    activo = document.getBoolean("activo") ?: true
                )
            }

            Result.success(consultants)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updateConsultantStatus(
        uid: String,
        activo: Boolean
    ): Result<Unit> {

        return try {

            firestore.collection("users")
                .document(uid)
                .update("activo", activo)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}