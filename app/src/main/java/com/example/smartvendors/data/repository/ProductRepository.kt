
package com.example.smartvendors.data.repository

import android.content.Context
import com.example.smartvendors.data.local.ProductLocalDataSource
import com.example.smartvendors.data.local.database.SmartVendorsDatabaseProvider
import com.example.smartvendors.data.local.entity.ProductEntity
import com.example.smartvendors.data.remote.RetrofitInstance
import com.example.smartvendors.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class ProductRepository(context: Context) {

    private val appContext = context.applicationContext
    private val api = RetrofitInstance.api

    private val localDataSource = ProductLocalDataSource(
        SmartVendorsDatabaseProvider
            .getDatabase(appContext)
            .productDao()
    )

    suspend fun getProducts(): Result<List<Product>> {

        val onlineProducts = try {
            api.getProducts().products
        } catch (e: Exception) {
            null
        }

        if (onlineProducts != null) {

            // Guardar información de productos en Room
            try {
                localDataSource.saveProducts(
                    onlineProducts.mapIndexed { index, product ->
                        product.toEntity(index)
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Guardar imágenes en archivos locales
            cacheProductImages(onlineProducts)

            return Result.success(onlineProducts)
        }

        // Recuperar productos desde Room si no hay internet
        return try {
            val cachedProducts = localDataSource.getProducts()

            if (cachedProducts.isNotEmpty()) {
                Result.success(
                    cachedProducts.map { it.toDomain() }
                )
            } else {
                Result.failure(
                    Exception("No hay conexión ni productos guardados.")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun cacheProductImages(products: List<Product>) {
        withContext(Dispatchers.IO) {

            val imageDirectory = File(
                appContext.filesDir,
                "product_images"
            )

            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs()
            }

            products.forEach { product ->

                try {
                    if (product.thumbnail.isBlank()) return@forEach

                    val imageFile = File(
                        imageDirectory,
                        "${product.id}.img"
                    )

                    // No descargar nuevamente si ya existe
                    if (imageFile.exists() && imageFile.length() > 0) {
                        return@forEach
                    }

                    val tempFile = File(
                        imageDirectory,
                        "${product.id}.tmp"
                    )

                    URL(product.thumbnail).openStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (tempFile.exists() && tempFile.length() > 0) {
                        tempFile.renameTo(imageFile)
                    }

                } catch (e: Exception) {
                    // Si falla una imagen, continuar con las demás
                    e.printStackTrace()
                }
            }
        }
    }

    private fun Product.toEntity(position: Int) = ProductEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        thumbnail = thumbnail,
        position = position
    )

    private fun ProductEntity.toDomain() = Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        thumbnail = thumbnail
    )
}
