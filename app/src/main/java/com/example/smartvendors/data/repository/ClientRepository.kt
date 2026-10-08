package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.Client
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClientRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun clientsCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("clientes")

    suspend fun getClients(uid: String): Result<List<Client>> {
        return try {

            val snapshot = clientsCollection(uid)
                .get()
                .await()

            val clients = snapshot.documents.map { document ->

                Client(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    apellido = document.getString("apellido") ?: "",
                    documento = document.getString("documento") ?: "",
                    telefono = document.getString("telefono") ?: "",
                    correo = document.getString("correo") ?: "",
                    direccion = document.getString("direccion") ?: ""
                )
            }

            Result.success(clients)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addClient(
        uid: String,
        client: Client
    ): Result<Unit> {
        return try {

            val document = clientsCollection(uid)
                .document()

            val data = hashMapOf(
                "nombre" to client.nombre,
                "apellido" to client.apellido,
                "documento" to client.documento,
                "telefono" to client.telefono,
                "correo" to client.correo,
                "direccion" to client.direccion
            )

            document.set(data).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClient(
        uid: String,
        client: Client
    ): Result<Unit> {
        return try {

            if (client.id.isBlank()) {
                return Result.failure(
                    Exception("El cliente no tiene un ID válido")
                )
            }

            val data = hashMapOf(
                "nombre" to client.nombre,
                "apellido" to client.apellido,
                "documento" to client.documento,
                "telefono" to client.telefono,
                "correo" to client.correo,
                "direccion" to client.direccion
            )

            clientsCollection(uid)
                .document(client.id)
                .set(data)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClient(
        uid: String,
        clientId: String
    ): Result<Unit> {
        return try {

            if (clientId.isBlank()) {
                return Result.failure(
                    Exception("El cliente no tiene un ID válido")
                )
            }

            clientsCollection(uid)
                .document(clientId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}