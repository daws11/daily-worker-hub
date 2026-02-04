package com.example.dwhubfix.di.module

import com.example.dwhubfix.core.network.SupabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Network module for Hilt dependency injection
 *
 * Provides network-related dependencies including the Supabase client singleton.
 * This module is installed in SingletonComponent to ensure single instance
 * throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the singleton SupabaseClient instance
     *
     * @return SupabaseClient wrapper containing the actual Supabase client
     */
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClient()
    }
}
