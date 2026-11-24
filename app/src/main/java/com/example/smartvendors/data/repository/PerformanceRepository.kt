package com.example.smartvendors.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PerformanceRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getPerformanceData(): Result<Map<String, Any>> {
        return try {

            val uid = auth.currentUser?.uid
                ?: return Result.failure(
                    Exception("No hay una consultora autenticada")
                )

            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("pedidos")
                .get()
                .await()

            val pedidos = snapshot.documents

            val totalPedidos = pedidos.size

            val ventasTotales = pedidos.sumOf { document ->
                document.getDouble("total") ?: 0.0
            }

            val pedidosPendientes = pedidos.count { document ->
                document.getString("estado") == "Pendiente"
            }

            val unidadesVendidas = pedidos.sumOf { document ->
                val items = document.get("items") as? List<Map<String, Any>>
                    ?: emptyList()

                items.sumOf { item ->
                    (item["quantity"] as? Number)?.toInt() ?: 0
                }
            }

            val datos = mapOf(
                "totalPedidos" to totalPedidos,
                "ventasTotales" to ventasTotales,
                "pedidosPendientes" to pedidosPendientes,
                "unidadesVendidas" to unidadesVendidas
            )

            Result.success(datos)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
