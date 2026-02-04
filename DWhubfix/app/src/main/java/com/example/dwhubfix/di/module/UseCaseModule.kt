package com.example.dwhubfix.di.module

import com.example.dwhubfix.domain.usecase.AcceptJobUseCase
import com.example.dwhubfix.domain.usecase.GetBusinessJobsUseCase
import com.example.dwhubfix.domain.usecase.GetBusinessStatsUseCase
import com.example.dwhubfix.domain.usecase.GetJobsForWorkerUseCase
import com.example.dwhubfix.domain.usecase.GetWorkerStatsUseCase
import com.example.dwhubfix.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing Use Cases
 *
 * This module provides all the use cases (interactors) used by ViewModels.
 * Use cases are reusable business logic components that should be singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * Provide LoginUseCase
     */
    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: com.example.dwhubfix.domain.repository.AuthRepository
    ): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    /**
     * Provide GetJobsForWorkerUseCase
     */
    @Provides
    @Singleton
    fun provideGetJobsForWorkerUseCase(
        jobRepository: com.example.dwhubfix.domain.repository.JobRepository
    ): GetJobsForWorkerUseCase {
        return GetJobsForWorkerUseCase(jobRepository)
    }

    /**
     * Provide AcceptJobUseCase
     */
    @Provides
    @Singleton
    fun provideAcceptJobUseCase(
        jobRepository: com.example.dwhubfix.domain.repository.JobRepository
    ): AcceptJobUseCase {
        return AcceptJobUseCase(jobRepository)
    }

    /**
     * Provide GetBusinessStatsUseCase
     */
    @Provides
    @Singleton
    fun provideGetBusinessStatsUseCase(
        jobRepository: com.example.dwhubfix.domain.repository.JobRepository
    ): GetBusinessStatsUseCase {
        return GetBusinessStatsUseCase(jobRepository)
    }

    /**
     * Provide GetWorkerStatsUseCase
     */
    @Provides
    @Singleton
    fun provideGetWorkerStatsUseCase(
        jobRepository: com.example.dwhubfix.domain.repository.JobRepository
    ): GetWorkerStatsUseCase {
        return GetWorkerStatsUseCase(jobRepository)
    }

    /**
     * Provide GetBusinessJobsUseCase
     */
    @Provides
    @Singleton
    fun provideGetBusinessJobsUseCase(
        jobRepository: com.example.dwhubfix.domain.repository.JobRepository
    ): GetBusinessJobsUseCase {
        return GetBusinessJobsUseCase(jobRepository)
    }
}
