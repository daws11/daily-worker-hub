package com.example.dwhubfix.di.module

import com.example.dwhubfix.data.repository.AuthRepositoryImpl
import com.example.dwhubfix.data.repository.BookingRepository
import com.example.dwhubfix.data.repository.BusinessMatchingRepository
import com.example.dwhubfix.data.repository.JobRepositoryImpl
import com.example.dwhubfix.data.repository.MatchingRepository
import com.example.dwhubfix.domain.repository.AuthRepository
import com.example.dwhubfix.domain.repository.JobRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Module for Hilt dependency injection
 *
 * Binds repository interfaces to their implementations.
 * This module is installed in SingletonComponent to ensure
 * single instance of each repository throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds AuthRepository interface to AuthRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Binds JobRepository interface to JobRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository

    // Concrete repositories (not interfaces yet, but using DI)
    // These will be converted to interfaces in Phase 2
    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepository: BookingRepository
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindMatchingRepository(
        matchingRepository: MatchingRepository
    ): MatchingRepository

    @Binds
    @Singleton
    abstract fun bindBusinessMatchingRepository(
        businessMatchingRepository: BusinessMatchingRepository
    ): BusinessMatchingRepository
}
