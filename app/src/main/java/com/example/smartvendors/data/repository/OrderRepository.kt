
package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
}
