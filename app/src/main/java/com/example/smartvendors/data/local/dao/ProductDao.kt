package com.example.smartvendors.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartvendors.data.local.entity.ProductEntity
import androidx.room.Transaction

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM productos ORDER BY position ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("DELETE FROM productos")
    suspend fun deleteAllProducts()

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun getProductCount(): Int

    @Transaction
    suspend fun replaceAllProducts(products: List<ProductEntity>) {
        deleteAllProducts()
        insertProducts(products)
    }
}
