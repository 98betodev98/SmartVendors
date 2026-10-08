package com.example.smartvendors.data.remote

import com.example.smartvendors.domain.model.Product
import retrofit2.http.GET

data class ProductResponse(
    val products: List<Product>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

interface ProductApiService {

    @GET("products")
    suspend fun getProducts(): ProductResponse
}