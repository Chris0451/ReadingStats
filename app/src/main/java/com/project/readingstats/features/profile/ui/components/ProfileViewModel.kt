// ProfileViewModel.kt
package com.project.readingstats.features.profile.ui.components

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.uid?.let { uid ->
                    val userProfile = firestoreUserDataSource.getUserProfile(uid)
                    _user.value = userProfile
                }
            } catch (e: Exception) {
                // Gestisci errori qui
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
