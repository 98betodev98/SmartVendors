package com.example.smartvendors.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartvendors.data.local.dao.ProductDao
import com.example.smartvendors.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SmartVendorsDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
}
