package com.example.smartvendors.data.local

import com.example.smartvendors.data.local.dao.ProductDao
import com.example.smartvendors.data.local.entity.ProductEntity

class ProductLocalDataSource(
    private val productDao: ProductDao
) {

    suspend fun saveProducts(products: List<ProductEntity>) {
        productDao.replaceAllProducts(products)
    }

    suspend fun getProducts(): List<ProductEntity> {
        return productDao.getAllProducts()
    }

    suspend fun hasProducts(): Boolean {
        return productDao.getProductCount() > 0
    }

    suspend fun clearProducts() {
        productDao.deleteAllProducts()
    }
}
