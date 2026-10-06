package com.example.smartvendors.data.repository

import com.example.smartvendors.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class ProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()


    private val usersCollection = firestore
        .collection("users")

    suspend fun getProfile(
        uid: String
    ): Result<UserProfile> {

        return try {

            val document = usersCollection
                .document(uid)
                .get()
                .await()

            if (document.exists()) {

                val profile = document.toObject(UserProfile::class.java)

                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(
                        Exception("No se pudo convertir el perfil")
                    )
                }

            } else {

                Result.success(
                    UserProfile(
                        uid = uid
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun saveProfile(
        profile: UserProfile
    ): Result<Unit> {

        return try {

            usersCollection
                .document(profile.uid)
                .set(profile)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }




}