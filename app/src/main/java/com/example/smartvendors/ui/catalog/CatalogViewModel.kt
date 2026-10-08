package com.example.smartvendors.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvendors.data.repository.ProductRepository
import com.example.smartvendors.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadProducts() {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getProducts()

            result
                .onSuccess { productList ->
                    _allProducts.value = productList
                    _products.value = productList
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al cargar los productos"
                }

            _isLoading.value = false
        }
    }

    fun searchProducts(query: String) {

        _searchQuery.value = query

        applyFilters()
    }

    fun filterByCategory(category: String) {

        _selectedCategory.value = category

        applyFilters()
    }

    private fun applyFilters() {

        val query = _searchQuery.value
        val category = _selectedCategory.value

        _products.value = _allProducts.value.filter { product ->

            val matchesSearch =
                query.isBlank() ||
                        product.title.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        product.category.contains(
                            query,
                            ignoreCase = true
                        )

            val matchesCategory =
                category == "Todas" ||
                        product.category.equals(
                            category,
                            ignoreCase = true
                        )

            matchesSearch && matchesCategory
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}