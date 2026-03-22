package com.rpn.salatetime.data.repository

import com.rpn.salatetime.data.local.datastore.SettingsRepository
import com.rpn.salatetime.domain.repository.AuthRepository
import com.rpn.salatetime.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber

class AuthRepositoryImpl(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Flow<Result<UserInfo>> = flow {
        emit(Result.Loading())
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val name = user.userMetadata
                    ?.get("full_name")
                    ?.toString()
                    ?.trim('"')
                    ?: ""

                settingsRepository.setUser(
                    id = user.id,
                    email = user.email ?: "",
                    userName = name
                )
                emit(Result.Success(data = user))
            } else {
                emit(Result.Error("Login failed. Please try again."))
            }
        } catch (e: AuthRestException) {
            Timber.w(e, "login: AuthRestException")
            emit(Result.Error(mapAuthError(e)))
        } catch (e: HttpRequestException) {
            Timber.w(e, "login: HttpRequestException")
            emit(Result.Error(mapHttpError(e)))
        } catch (e: Exception) {
            Timber.e(e, "login: unexpected error")
            emit(Result.Error("An unexpected error occurred. Check your connection and try again."))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Result<UserInfo>> = flow {
        emit(Result.Loading())
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("full_name", name) }
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                settingsRepository.setUser(
                    id = user.id,
                    email = user.email ?: "",
                    userName = name
                )
                emit(Result.Success(data = user))
            } else {
                // Email confirmation is enabled in Supabase — session not created yet
                emit(Result.Error("Registration successful. Please verify your email before logging in."))
            }
        } catch (e: AuthRestException) {
            Timber.w(e, "register: AuthRestException")
            emit(Result.Error(mapAuthError(e)))
        } catch (e: HttpRequestException) {
            Timber.w(e, "register: HttpRequestException")
            emit(Result.Error(mapHttpError(e)))
        } catch (e: Exception) {
            Timber.e(e, "register: unexpected error")
            emit(Result.Error("Registration failed. Check your connection and try again."))
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            Timber.w(e, "logout: sign-out call failed, clearing local state anyway")
        } finally {
            // Always clear local data regardless of whether the network call succeeded
            settingsRepository.clearUserData()
        }
    }

    override fun getCurrentUser(): Flow<UserInfo?> = flow {
        emit(supabase.auth.currentUserOrNull())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps Supabase [AuthRestException] error codes / messages into human-readable
     * strings that are safe to show directly in the UI.
     *
     * Supabase error codes reference:
     * https://supabase.com/docs/reference/kotlin/auth-error-codes
     */
    private fun mapAuthError(e: AuthRestException): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "invalid login credentials" in msg || "invalid_credentials" in msg ->
                "Incorrect email or password. Please try again."

            "email not confirmed" in msg || "email_not_confirmed" in msg ->
                "Please verify your email address before logging in."

            "user already registered" in msg ->
                "An account with this email already exists. Try logging in instead."

            "password should be" in msg ->
                "Password is too weak. Use at least 6 characters."

            "rate limit" in msg || "over_email_send_rate_limit" in msg ->
                "Too many attempts. Please wait a moment and try again."

            "network" in msg || "unable to connect" in msg ->
                "No internet connection. Please check your network."

            else -> e.message ?: "Authentication failed. Please try again."
        }
    }

    /**
     * Maps HTTP request exceptions, including SSL certificate validation errors.
     */
    private fun mapHttpError(e: HttpRequestException): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "chain validation failed" in msg ->
                "SSL certificate validation failed. Please check your internet connection or try again later."

            "ssl" in msg || "certificate" in msg ->
                "SSL connection error. Please check your network settings."

            "timeout" in msg || "timed out" in msg ->
                "Connection timed out. Please check your internet connection."

            "network" in msg || "unable to connect" in msg || "no address associated" in msg ->
                "No internet connection. Please check your network."

            else -> "Network error occurred. Please check your connection and try again."
        }
    }
}