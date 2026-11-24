
package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.Consultant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ConsultantRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun consultantsCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("consultoras")

    // CONSULTAR CONSULTORAS
    suspend fun getConsultants(
        uid: String
    ): Result<List<Consultant>> {
        return try {

            val snapshot = consultantsCollection(uid)
                .get()
                .await()

            val consultants = snapshot.documents.map { document ->
                Consultant(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    apellido = document.getString("apellido") ?: "",
                    documento = document.getString("documento") ?: "",
                    telefono = document.getString("telefono") ?: "",
                    correo = document.getString("correo") ?: "",
                    direccion = document.getString("direccion") ?: ""
                )
            }

            Result.success(consultants)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // REGISTRAR CONSULTORA
    suspend fun addConsultant(
        uid: String,
        consultant: Consultant
    ): Result<Unit> {
        return try {

            val document = consultantsCollection(uid)
                .document()

            val data = hashMapOf(
                "nombre" to consultant.nombre,
                "apellido" to consultant.apellido,
                "documento" to consultant.documento,
                "telefono" to consultant.telefono,
                "correo" to consultant.correo,
                "direccion" to consultant.direccion
            )

            document.set(data).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ACTUALIZAR CONSULTORA
    suspend fun updateConsultant(
        uid: String,
        consultant: Consultant
    ): Result<Unit> {
        return try {

            if (consultant.id.isBlank()) {
                return Result.failure(
                    Exception("La consultora no tiene un ID válido")
                )
            }

            val data = hashMapOf(
                "nombre" to consultant.nombre,
                "apellido" to consultant.apellido,
                "documento" to consultant.documento,
                "telefono" to consultant.telefono,
                "correo" to consultant.correo,
                "direccion" to consultant.direccion
            )

            consultantsCollection(uid)
                .document(consultant.id)
                .set(data)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
