package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.InitialSetup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InitialSetupRepository {

    private val db =
        FirebaseFirestore.getInstance()

    suspend fun saveSetup(
        setup: InitialSetup
    ): Result<Unit> {

        return try {

            db.collection("users")
                .document(setup.uid)
                .collection("configuracion")
                .document("inicial")
                .set(setup)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getSetup(
        uid: String
    ): Result<InitialSetup?> {

        return try {

            val document =
                db.collection("users")
                    .document(uid)
                    .collection("configuracion")
                    .document("inicial")
                    .get()
                    .await()

            if (document.exists()) {

                val setup =
                    document.toObject(
                        InitialSetup::class.java
                    )

                Result.success(setup)

            } else {

                Result.success(null)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun isSetupCompleted(
        uid: String
    ): Result<Boolean> {

        return try {

            val document =
                db.collection("users")
                    .document(uid)
                    .collection("configuracion")
                    .document("inicial")
                    .get()
                    .await()

            if (!document.exists()) {
                Result.success(false)
            } else {

                val completed =
                    document.getBoolean(
                        "configuracionCompletada"
                    ) ?: false

                Result.success(completed)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}