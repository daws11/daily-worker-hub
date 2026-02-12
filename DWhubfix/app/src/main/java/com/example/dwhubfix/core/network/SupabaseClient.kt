package com.example.dwhubfix.core.network

import com.example.dwhubfix.BuildConfig
import io.github.jan.supabase.SupabaseClient as SupabaseClientInstance
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton Supabase Client wrapper
 *
 * This class provides a single instance of the Supabase client that can be
 * injected throughout the application. It ensures that we maintain a single
 * connection to Supabase and follow proper dependency injection patterns.
 *
 * Usage:
 * ```
 * @Inject constructor(
 *     private val supabaseClient: SupabaseClient
 * ) {
 *     val client = supabaseClient.client
 * }
 * ```
 */
@Singleton
class SupabaseClient @Inject constructor() {

    /**
     * The actual Supabase client instance
     * Use this property to access Supabase features
     */
    val client: SupabaseClientInstance = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
