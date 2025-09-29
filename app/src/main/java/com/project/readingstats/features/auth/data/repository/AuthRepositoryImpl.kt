package com.project.readingstats.features.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.project.readingstats.features.auth.data.source.FirebaseAuthDataSource
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.auth.domain.repository.AuthRepository
import com.project.readingstats.features.auth.domain.repository.RegisterResult
import com.project.readingstats.features.auth.data.model.UserModelDto
import com.project.readingstats.features.auth.domain.repository.LoginResult
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

    override suspend fun login(
        email: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {
        try{
            auth.signIn(email, password)
            val uid = auth.currentUid() ?: return@withContext LoginResult.Error("AUTH_ERROR", "User ID mancante")

            val exists = store.userExists(uid)
            if(!exists) {
                auth.signOut()
                return@withContext LoginResult.Error("USER_NOT_FOUND", "Utente non trovato")
            }
            LoginResult.Success
        } catch (e: FirebaseAuthException) {
            LoginResult.Error(e.errorCode ?: "AUTH_ERROR", e.message)
        } catch (e: FirebaseNetworkException) {
            LoginResult.Error("NETWORK", "Problema di rete, riprova")
        } catch (e: Exception) {
            val fe = e as? FirebaseAuthException
            val code = fe?.errorCode ?: e::class.qualifiedName.orEmpty()
            LoginResult.Error(code.ifBlank { "GENERIC" }, e.message ?: e.toString())
        }
    }

    private fun mapFirebaseAuthError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> "ERROR_USER_NOT_FOUND"
        is FirebaseAuthInvalidCredentialsException -> "ERROR_WRONG_PASSWORD"
        is FirebaseAuthUserCollisionException -> "EMAIL_IN_USE"
        is FirebaseNetworkException -> "NETWORK"
        is FirebaseAuthException -> e.errorCode.ifBlank { "AUTH_EXCEPTION" }
        else -> e::class.qualifiedName.orEmpty()
    }
}