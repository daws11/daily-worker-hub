package com.example.dwhubfix.di.module

import com.example.dwhubfix.data.repository.AuthRepositoryImpl
import com.example.dwhubfix.data.repository.JobRepositoryImpl
import com.example.dwhubfix.domain.repository.AuthRepository
import com.example.dwhubfix.domain.repository.JobRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Module
 *
 * Provides repository instances to dependency injection container.
 * Binds interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    /**
     * Provide Auth Repository implementation
     *
     * @return AuthRepositoryImpl instance
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository {
        return authRepositoryImpl
    }

    /**
     * Provide Job Repository implementation
     *
     * @return JobRepositoryImpl instance
     */
    @Provides
    @Singleton
    fun provideJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository {
        return jobRepositoryImpl
    }
}
