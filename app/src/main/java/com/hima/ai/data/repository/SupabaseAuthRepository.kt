package com.hima.ai.data.repository

import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.AppError
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.core.common.safeApiCallNoBody
import com.hima.ai.data.remote.supabase.PostgrestErrorResponse
import com.hima.ai.data.remote.supabase.ProfileInsertRequest
import com.hima.ai.data.remote.supabase.SupabaseAuthApi
import com.hima.ai.data.remote.supabase.SupabaseCredentialsRequest
import com.hima.ai.data.remote.supabase.SupabaseErrorResponse
import com.hima.ai.data.remote.supabase.SupabaseRestApi
import com.hima.ai.domain.model.AuthSession
import com.hima.ai.domain.model.User
import com.hima.ai.domain.model.UserRole
import com.hima.ai.domain.repository.AuthRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val authApi: SupabaseAuthApi,
    private val restApi: SupabaseRestApi,
    private val moshi: Moshi,
) : AuthRepository {

    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    override val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()

    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): ApiResult<AuthSession> {
        val signUpResult = moshi.safeApiCall(::parseSupabaseError, label = "supabase/auth") {
            authApi.signUp(SupabaseCredentialsRequest(email, password))
        }
        val signUpBody = when (signUpResult) {
            is ApiResult.Failure -> return signUpResult
            is ApiResult.Success -> signUpResult.value
        }

        val userId = signUpBody.user?.id ?: signUpBody.id
        val accessToken = signUpBody.accessToken
        val refreshToken = signUpBody.refreshToken
        if (userId == null || accessToken == null || refreshToken == null) {
            // This project requires confirming the address by email before a
            // session exists — signup succeeded, but there is nothing to sign
            // the ranger into yet, and this app has no "check your email" step.
            return ApiResult.Failure(
                AppError.Rejected("Account created, but email confirmation is required before signing in."),
            )
        }

        val bearer = "Bearer $accessToken"
        val profileResult = moshi.safeApiCallNoBody(::parsePostgrestError, label = "supabase/profile") {
            restApi.insertProfile(bearer, ProfileInsertRequest(id = userId, fullName = fullName, role = role.wireValue))
        }
        if (profileResult is ApiResult.Failure) return profileResult

        val session = AuthSession(
            user = User(id = userId, email = signUpBody.user?.email ?: signUpBody.email ?: email, fullName = fullName, role = role),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        _currentSession.value = session
        return ApiResult.Success(session)
    }

    override suspend fun login(email: String, password: String): ApiResult<AuthSession> {
        val loginResult = moshi.safeApiCall(::parseSupabaseError, label = "supabase/auth") {
            authApi.login(body = SupabaseCredentialsRequest(email, password))
        }
        val loginBody = when (loginResult) {
            is ApiResult.Failure -> return loginResult
            is ApiResult.Success -> loginResult.value
        }

        val userId = loginBody.user?.id
        val accessToken = loginBody.accessToken
        val refreshToken = loginBody.refreshToken
        if (userId == null || accessToken == null || refreshToken == null) {
            return ApiResult.Failure(AppError.Unexpected("Sign-in succeeded but returned no session."))
        }

        val bearer = "Bearer $accessToken"
        val profileResult = moshi.safeApiCall(::parsePostgrestError, label = "supabase/profile") {
            restApi.getProfile(bearer, idFilter = "eq.$userId")
        }
        val profiles = when (profileResult) {
            is ApiResult.Failure -> return profileResult
            is ApiResult.Success -> profileResult.value
        }
        val profile = profiles.firstOrNull()
            ?: return ApiResult.Failure(AppError.Unexpected("No profile found for this account."))
        val role = UserRole.fromWireValue(profile.role)
            ?: return ApiResult.Failure(AppError.Unexpected("Unknown account role: ${profile.role}"))

        val session = AuthSession(
            user = User(id = userId, email = loginBody.user.email ?: email, fullName = profile.fullName, role = role),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        _currentSession.value = session
        return ApiResult.Success(session)
    }

    override suspend fun logout(): ApiResult<Unit> {
        val token = _currentSession.value?.accessToken
        // Already signed out locally; nothing to invalidate server-side.
        if (token == null) {
            return ApiResult.Success(Unit)
        }
        val result = moshi.safeApiCallNoBody(::parseSupabaseError, label = "supabase/auth") { authApi.logout("Bearer $token") }
        // Clear locally regardless of server outcome — an unreachable server
        // shouldn't be able to trap the ranger in a "signed in" state.
        _currentSession.value = null
        return result
    }

    private fun parseSupabaseError(raw: String): String? =
        runCatching { moshi.adapter(SupabaseErrorResponse::class.java).fromJson(raw)?.readableMessage() }.getOrNull()

    private fun parsePostgrestError(raw: String): String? =
        runCatching { moshi.adapter(PostgrestErrorResponse::class.java).fromJson(raw)?.message }.getOrNull()
}
