package com.example.smartvendors.data.repository

import com.example.smartvendors.data.remote.RetrofitInstance
import com.example.smartvendors.domain.model.Product

class ProductRepository {

    private val api = RetrofitInstance.api

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = api.getProducts()

            Result.success(response.products)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}