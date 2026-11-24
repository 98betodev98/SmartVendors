package com.example.smartvendors.data.local.database

import android.content.Context
import androidx.room.Room

object SmartVendorsDatabaseProvider {

    @Volatile
    private var INSTANCE: SmartVendorsDatabase? = null

    fun getDatabase(context: Context): SmartVendorsDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                SmartVendorsDatabase::class.java,
                "smartvendors_database"
            ).fallbackToDestructiveMigration().build()

            INSTANCE = instance

            instance
        }
    }
}
