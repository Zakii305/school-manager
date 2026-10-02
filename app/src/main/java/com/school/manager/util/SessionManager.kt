package com.school.manager.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Global session state — holds the current user's context.
 * Every ViewModel reads from this to filter Firestore queries.
 */
object SessionManager {

    data class Session(
        val uid: String = "",
        val name: String = "",
        val email: String = "",
        val role: String = "",
        val schoolId: String = "",
        val isOwner: Boolean = false
    )

    var current by mutableStateOf(Session())
        private set

    fun set(session: Session) {
        current = session
    }

    fun clear() {
        current = Session()
    }

    /** Returns the schoolId that queries should filter by.
     *  Owners see all schools — they pass `null` to skip the filter. */
    fun querySchoolId(): String? = if (current.isOwner) null else current.schoolId
}
