package com.project.readingstats.features.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.project.readingstats.features.auth.data.source.FirebaseAuthDataSource
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.auth.domain.repository.AuthRepository
import com.project.readingstats.features.auth.domain.repository.RegisterResult
import com.project.readingstats.features.auth.data.model.UserModelDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val auth: FirebaseAuthDataSource,
    private val store: FirestoreUserDataSource
): AuthRepository {
    override suspend fun isUsernameAvailable(username: String): Boolean =
        withContext(Dispatchers.IO){
            !store.isUsernameTaken(username.trim().lowercase())
        }

    override suspend fun register(
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String
    ): RegisterResult = withContext(Dispatchers.IO) {
        try{
            val result = auth.createUser(email, password)
            val uid = result.user?.uid ?: return@withContext RegisterResult.Error("NO_UID", "User ID mancante")
            val now = System.currentTimeMillis()
            val dto = UserModelDto(
                uid = uid,
                name = name.trim(),
                surname = surname.trim(),
                username = username.trim(),
                email = email.trim(),
                createdAt = now
            )
            store.createUserAtomically(
                uid = uid,
                profile = mapOf(
                    "uid" to dto.uid,
                    "name" to dto.name,
                    "surname" to dto.surname,
                    "username" to dto.username,
                    "email" to dto.email,
                    "createdAt" to dto.createdAt
                )
            )
            RegisterResult.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            RegisterResult.Error("EMAIL_IN_USE", e.message)
        }catch (e: FirebaseAuthException) {
            // <-- Qui hai il codice preciso (es. CONFIGURATION_NOT_FOUND)
            RegisterResult.Error(e.errorCode ?: "AUTH_ERROR", e.message)
        }
        catch (e: IllegalStateException) {
            RegisterResult.Error(e.message ?: "USERNAME_TAKEN", "Username già in uso")
        } catch (e: FirebaseNetworkException) {
            RegisterResult.Error("NETWORK", e.message)
        } catch (e: Exception) {
            val fe = e as? com.google.firebase.auth.FirebaseAuthException
            val code = fe?.errorCode ?: e::class.qualifiedName.orEmpty()
            RegisterResult.Error(code.ifBlank { "GENERIC" }, e.message ?: e.toString())
        }
    }
}