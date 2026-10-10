package com.example.smartvendors.ui.cart

import androidx.lifecycle.ViewModel
import com.example.smartvendors.data.repository.CartRepository
import com.example.smartvendors.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {

    private val repository = CartRepository()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    fun addProduct(item: CartItem) {
        repository.addProduct(item)
        refreshCart()
    }

    fun increaseQuantity(productId: Int) {
        repository.increaseQuantity(productId)
        refreshCart()
    }

    fun decreaseQuantity(productId: Int) {
        repository.decreaseQuantity(productId)
        refreshCart()
    }

    fun removeProduct(productId: Int) {
        repository.removeProduct(productId)
        refreshCart()
    }

    fun clearCart() {
        repository.clearCart()
        refreshCart()
    }

    fun loadCart() {
        refreshCart()
    }

    private fun refreshCart() {
        _cartItems.value = repository.getCartItems()
        _total.value = repository.getTotal()
    }
}