package com.school.manager.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Loads school branding (name, tagline, contact) once and exposes it
 * to any composable that needs it. Cached in-memory.
 */
object BrandingHelper {

    data class Branding(
        val name: String = "School Manager",
        val tagline: String = "SCHOOL MANAGEMENT",
        val phone: String = "",
        val email: String = "",
        val address: String = ""
    )

    private val _state = MutableStateFlow(Branding())
    val state: StateFlow<Branding> = _state.asStateFlow()

    private var loaded = false

    suspend fun load() {
        if (loaded) return
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("settings").document("branding").get().await()
            _state.value = Branding(
                name = doc.getString("name") ?: "School Manager",
                tagline = doc.getString("tagline") ?: "SCHOOL MANAGEMENT",
                phone = doc.getString("phone") ?: "",
                email = doc.getString("email") ?: "",
                address = doc.getString("address") ?: ""
            )
            loaded = true
        } catch (_: Exception) { }
    }

    fun refresh() {
        loaded = false
    }
}
