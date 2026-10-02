package com.example.smartvendors.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun register(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {

            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: return Result.failure(
                    Exception("No se pudo obtener el usuario registrado")
                )

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: return Result.failure(
                    Exception("No se pudo obtener el usuario")
                )

            if (!user.isEmailVerified) {
                return Result.failure(
                    Exception("Debes verificar tu correo electrónico antes de iniciar sesión")
                )
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(
                    Exception("No hay un usuario autenticado")
                )

            user.sendEmailVerification().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailVerification(): Result<Boolean> {
        return try {

            val user = auth.currentUser
                ?: return Result.failure(
                    Exception("No hay un usuario autenticado")
                )

            user.reload().await()

            Result.success(user.isEmailVerified)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(
        idToken: String
    ): Result<FirebaseUser> {
        return try {

            val credential =
                com.google.firebase.auth.GoogleAuthProvider
                    .getCredential(idToken, null)

            val result = auth
                .signInWithCredential(credential)
                .await()

            val user = result.user
                ?: return Result.failure(
                    Exception("No se pudo obtener el usuario de Google")
                )

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }


}