package com.project.readingstats.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.features.auth.data.model.UserModelDto
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val firestoreUserDataSource: FirestoreUserDataSource
) : ViewModel() {

    private val _user = MutableStateFlow<UserModelDto?>(null)
    val user: StateFlow<UserModelDto?> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Stato per gestire il salvataggio degli aggiornamenti
    private val _updateLoading = MutableStateFlow(false)
    val updateLoading: StateFlow<Boolean> = _updateLoading

    // Stato per gestire il risultato dell'aggiornamento
    private val _updateResult = MutableStateFlow<String?>(null)
    val updateResult: StateFlow<String?> = _updateResult

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null) {
                    val userProfile = firestoreUserDataSource.getUserProfile(currentUid)
                    _user.value = userProfile
                }
            } catch (e: Exception) {
                // Gestisci errore se necessario
            } finally {
                _loading.value = false
            }
        }
    }

    // Funzione per aggiornare i dati utente
    fun updateUserProfile(
        username: String,
        name: String,
        surname: String,
        email: String
    ) {
        viewModelScope.launch {
            _updateLoading.value = true // Inizia il caricamento
            _updateResult.value = null // Reset del risultato precedente

            try {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null) {
                    // Chiama il datasource per aggiornare i dati su Firestore
                    firestoreUserDataSource.updateUserProfile(
                        uid = currentUid,
                        username = username,
                        name = name,
                        surname = surname,
                        email = email
                    )

                    // Aggiorna lo stato locale con i nuovi dati
                    _user.value = _user.value?.copy(
                        username = username,
                        name = name,
                        surname = surname,
                        email = email
                    )

                    _updateResult.value = "success" // Aggiornamento riuscito
                } else {
                    _updateResult.value = "error_no_user" // Utente non trovato
                }
            } catch (e: Exception) {
                _updateResult.value = "error_update_failed" // Errore durante l'aggiornamento
            } finally {
                _updateLoading.value = false // Fine del caricamento
            }
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }

    // Funzione per resettare il risultato dell'aggiornamento
    fun clearUpdateResult() {
        _updateResult.value = null
    }

}