package com.hima.ai.domain.repository

import com.hima.ai.core.common.ApiResult
import com.hima.ai.domain.model.AuthSession
import com.hima.ai.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

/**
 * Account creation, sign-in, and sign-out against Supabase Auth directly —
 * the anon key is client-safe by design, so nothing routes through our own
 * backend for this. [currentSession] is the single source of truth for
 * whether the app is authenticated; it holds in memory only, so it is cleared
 * by a process restart the same way [com.hima.ai.data.mock.PrototypeSession]
 * already is elsewhere in this prototype.
 */
interface AuthRepository {
    val currentSession: StateFlow<AuthSession?>

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): ApiResult<AuthSession>

    suspend fun login(email: String, password: String): ApiResult<AuthSession>

    suspend fun logout(): ApiResult<Unit>
}
