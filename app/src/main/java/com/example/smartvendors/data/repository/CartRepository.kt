package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.CartItem

class CartRepository {

    private val cartItems = mutableListOf<CartItem>()

    fun getCartItems(): List<CartItem> {
        return cartItems.toList()
    }

    fun addProduct(item: CartItem) {
        val existingItem = cartItems.find {
            it.productId == item.productId
        }

        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)

            cartItems[index] = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
        } else {
            cartItems.add(item)
        }
    }

    fun increaseQuantity(productId: Int) {
        val index = cartItems.indexOfFirst {
            it.productId == productId
        }

        if (index != -1) {
            val item = cartItems[index]

            cartItems[index] = item.copy(
                quantity = item.quantity + 1
            )
        }
    }

    fun decreaseQuantity(productId: Int) {
        val index = cartItems.indexOfFirst {
            it.productId == productId
        }

        if (index != -1) {
            val item = cartItems[index]

            if (item.quantity > 1) {
                cartItems[index] = item.copy(
                    quantity = item.quantity - 1
                )
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    fun removeProduct(productId: Int) {
        cartItems.removeAll {
            it.productId == productId
        }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getTotal(): Double {
        return cartItems.sumOf {
            it.price * it.quantity
        }
    }
}