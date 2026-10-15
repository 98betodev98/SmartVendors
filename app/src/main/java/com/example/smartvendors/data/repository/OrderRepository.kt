
package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.FirebaseAuth

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveOrder(order: Order): Result<String> {
        return try {
            if (order.uid.isBlank()) {
                return Result.failure(
                    Exception("No hay un usuario válido para el pedido")
                )
            }

            if (order.items.isEmpty()) {
                return Result.failure(
                    Exception("El carrito está vacío")
                )
            }

            val orderData = hashMapOf(
                "uid" to order.uid,
                "total" to order.total,
                "fecha" to order.fecha,
                "estado" to order.estado,
                "items" to order.items.map { item ->
                    hashMapOf(
                        "productId" to item.productId,
                        "title" to item.title,
                        "price" to item.price,
                        "thumbnail" to item.thumbnail,
                        "quantity" to item.quantity,
                        "subtotal" to item.price * item.quantity
                    )
                }
            )

            val document = db.collection("users")
                .document(order.uid)
                .collection("pedidos")
                .document()

            document.set(orderData).await()

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersByUser(uid: String): Result<List<Order>> {
        return try {
            if (uid.isBlank()) {
                return Result.failure(
                    Exception("No hay un usuario válido para consultar los pedidos")
                )
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("pedidos")
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                try {
                    val itemsData = document.get("items")
                            as? List<Map<String, Any>> ?: emptyList()

                    val items = itemsData.mapNotNull { item ->
                        try {
                            com.example.smartvendors.domain.model.CartItem(
                                productId = (item["productId"] as? Number)?.toInt() ?: 0,
                                title = item["title"] as? String ?: "",
                                price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                                thumbnail = item["thumbnail"] as? String ?: "",
                                quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    Order(
                        id = document.id,
                        uid = document.getString("uid") ?: uid,
                        items = items,
                        total = document.getDouble("total") ?: 0.0,
                        fecha = document.getLong("fecha")
                            ?: System.currentTimeMillis(),
                        estado = document.getString("estado") ?: "Pendiente"
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(
                orders.sortedByDescending { it.fecha }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
