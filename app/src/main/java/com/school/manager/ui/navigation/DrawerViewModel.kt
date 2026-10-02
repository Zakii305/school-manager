package com.school.manager.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.BrandingHelper
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DrawerUser(
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val avatarUrl: String = ""
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _user = MutableStateFlow(DrawerUser())
    val user: StateFlow<DrawerUser> = _user.asStateFlow()

    val branding: StateFlow<BrandingHelper.Branding> = BrandingHelper.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, BrandingHelper.Branding())

    init {
        viewModelScope.launch { BrandingHelper.load() }
        load()
    }

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _user.value = DrawerUser()
            return
        }
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                _user.value = DrawerUser(
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
                    role = doc.getString("role") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: ""
                )
            } catch (_: Exception) { }
        }
    }
}
