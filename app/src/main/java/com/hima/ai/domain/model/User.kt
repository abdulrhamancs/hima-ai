package com.hima.ai.domain.model

/** Matches the backend's two account kinds (`field_agent` / `authority`). */
enum class UserRole(val wireValue: String) {
    FIELD_AGENT("field_agent"),
    AUTHORITY("authority"),
    ;

    companion object {
        fun fromWireValue(value: String): UserRole? = entries.find { it.wireValue == value }
    }
}

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
)

/** An authenticated session: the user plus the token used to call the backend. */
data class AuthSession(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)
