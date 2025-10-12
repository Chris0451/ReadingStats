package com.project.readingstats.features.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.project.readingstats.features.auth.data.source.FirebaseAuthDataSource
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.auth.domain.repository.AuthRepository
import com.project.readingstats.features.auth.domain.repository.RegisterResult
import com.project.readingstats.features.auth.data.model.UserModelDto
import com.project.readingstats.features.auth.domain.repository.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Implementazione concreta dell'interfaccia AuthRepository
class AuthRepositoryImpl(
    private val auth: FirebaseAuthDataSource,         // Gestisce operazioni di autenticazione Firebase
    private val store: FirestoreUserDataSource       // Gestisce lettura/scrittura su Firestore
): AuthRepository {

    // Verifica se username è disponibile (non già preso)
    override suspend fun isUsernameAvailable(username: String): Boolean =
        withContext(Dispatchers.IO) {                    // Esegue su thread pool IO
            !store.isUsernameTaken(username.trim().lowercase())  // Chiama FirestoreUserDataSource e nega il risultato
        }

    // Registra un nuovo utente (con nome, username, email, password)
    override suspend fun register(
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String
    ): RegisterResult = withContext(Dispatchers.IO) {
        try {
            // Crea utente su Firebase Auth con email e password
            val result = auth.createUser(email, password)

            // Prende uid utente appena creato, se mancante ritorna errore
            val uid = result.user?.uid ?: return@withContext RegisterResult.Error("NO_UID", "User ID mancante")

            val now = System.currentTimeMillis()          // Timestamp corrente

            // Costruisce DTO utente con dati passati
            val dto = UserModelDto(
                uid = uid,
                name = name.trim(),
                surname = surname.trim(),
                username = username.trim(),
                email = email.trim(),
                createdAt = now
            )

            // Salva l'utente atomico in Firestore tramite mappa di proprietà
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

            RegisterResult.Success                      // Se tutto ok ritorna successo
        } catch (e: FirebaseAuthUserCollisionException) {
            // Errore: email già in uso
            RegisterResult.Error("EMAIL_IN_USE", e.message)
        } catch (e: FirebaseAuthException) {
            // Errore generico di autenticazione Firebase
            RegisterResult.Error(e.errorCode ?: "AUTH_ERROR", e.message)
        } catch (e: IllegalStateException) {
            // Errore username già in uso (gestito in createUserAtomically)
            RegisterResult.Error(e.message ?: "USERNAME_TAKEN", "Username già in uso")
        } catch (e: FirebaseNetworkException) {
            // Errore di rete
            RegisterResult.Error("NETWORK", e.message)
        } catch (e: Exception) {
            // Errore generico non gestito utlizzando il tipo dell'eccezione
            val fe = e as? FirebaseAuthException
            val code = fe?.errorCode ?: e::class.qualifiedName.orEmpty()
            RegisterResult.Error(code.ifBlank { "GENERIC" }, e.message ?: e.toString())
        }
    }

    // Effettua login con email e password
    override suspend fun login(
        email: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {
        try {
            auth.signIn(email, password)                // Firebase Auth login

            val uid = auth.currentUid()                   // Prende uid corrente
                ?: return@withContext LoginResult.Error("AUTH_ERROR", "User ID mancante")

            val exists = store.userExists(uid)             // Verifica se esiste il profilo Firestore

            if (!exists) {                                 // Se non trovato
                auth.signOut()                             // Effettua logout
                return@withContext LoginResult.Error("USER_NOT_FOUND", "Utente non trovato")
            }

            LoginResult.Success                            // Login e profilo trovato: successo
        } catch (e: FirebaseAuthInvalidUserException) {
            // Errore utente inesistente o disabilitato
            val code = when (e.errorCode) {
                "USER_DISABLED", "user-disabled" -> "USER_DISABLED"
                else -> "USER_NOT_FOUND"
            }
            LoginResult.Error(code, e.message)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Errore email/password errati o malformati
            val code = when (e.errorCode) {
                "INVALID_EMAIL", "invalid-email" -> "INVALID_EMAIL"
                "WRONG_PASSWORD", "wrong-password" -> "WRONG_PASSWORD"
                else -> "INVALID_CREDENTIALS"
            }
            LoginResult.Error(code, e.message)
        } catch (e: FirebaseAuthUserCollisionException) {
            // Errore email già usata (raro in login ma incluso)
            LoginResult.Error("EMAIL_IN_USE", e.message)
        } catch (e: FirebaseTooManyRequestsException) {
            // Troppi tentativi
            LoginResult.Error("TOO_MANY_REQUESTS", e.message)
        } catch (e: FirebaseNetworkException) {
            // Errore di rete
            LoginResult.Error("NETWORK", e.message)
        } catch (e: FirebaseAuthException) {
            // Gestione codici comuni di errore autenticazione
            val mapped = when (e.errorCode) {
                "INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_CREDENTIALS" -> "INVALID_CREDENTIALS"
                "ERROR_INVALID_EMAIL", "invalid-email" -> "INVALID_EMAIL"
                "ERROR_WRONG_PASSWORD", "wrong-password" -> "WRONG_PASSWORD"
                "ERROR_USER_DISABLED", "user-disabled" -> "USER_DISABLED"
                "ERROR_USER_NOT_FOUND", "user-not-found", "EMAIL_NOT_FOUND" -> "USER_NOT_FOUND"
                else -> e.errorCode.ifBlank { "AUTH_ERROR" }
            }
            return@withContext LoginResult.Error(mapped, e.message ?: "Errore di autenticazione")
        } catch (e: Exception) {
            // Errore generico
            val msg = e.message ?: e.toString()
            if (msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true)) {
                return@withContext LoginResult.Error("INVALID_CREDENTIALS", msg)
            }
            return@withContext LoginResult.Error("GENERIC", msg)
        }
    }

    // In AuthRepositoryImpl.kt
    override suspend fun getCurrentUserProfile(): UserModelDto? = withContext(Dispatchers.IO) {
        val uid = auth.currentUid() ?: return@withContext null
        return@withContext store.getUserProfile(uid)
    }


}
