package com.example.smartvendors.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Registrar usuario en Firebase Authentication
    // y crear su documento en Firestore
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

            val userDocument = firestore.collection("users")
                .document(user.uid)

            val existingDocument = userDocument.get().await()

            if (!existingDocument.exists()) {
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "rol" to "USER"
                )

                userDocument.set(userData).await()
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Iniciar sesión con correo y contraseña
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

            // Validar correo verificado
            if (!user.isEmailVerified) {
                auth.signOut()

                return Result.failure(
                    Exception(
                        "Debes verificar tu correo electrónico antes de iniciar sesión"
                    )
                )
            }

            // Consultar estado de la cuenta en Firestore
            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            // Por defecto, usuarios antiguos se consideran activos
            val activo = document.getBoolean("activo") ?: true

            // Bloquear cuentas inactivas
            if (!activo) {

                auth.signOut()

                return Result.failure(
                    Exception(
                        "Tu cuenta ha sido desactivada. Contacta con el administrador."
                    )
                )
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener el rol del usuario autenticado
    suspend fun getUserRole(uid: String): Result<String> {
        return try {

            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val role = document.getString("rol")

            if (role == "ADMIN" || role == "USER") {
                Result.success(role)
            } else {
                Result.failure(
                    Exception("El usuario no tiene un rol válido asignado")
                )
            }

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

            // Obtener el documento del usuario
            val userDocument = firestore.collection("users")
                .document(user.uid)

            val existingDocument = userDocument.get().await()

            // Crear perfil solamente si todavía no existe
            if (!existingDocument.exists()) {

                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "nombre" to (user.displayName ?: ""),
                    "fotoPerfil" to (user.photoUrl?.toString() ?: ""),
                    "rol" to "USER",
                    "activo" to true
                )

                userDocument.set(userData).await()
            }

            // Consultar el estado de la cuenta
            val document = userDocument.get().await()

            val activo = document.getBoolean("activo") ?: true

            // Bloquear usuarios desactivados
            if (!activo) {

                auth.signOut()

                return Result.failure(
                    Exception(
                        "Tu cuenta ha sido desactivada. Contacta con el administrador."
                    )
                )
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun logout() {
        auth.signOut()
    }
}
