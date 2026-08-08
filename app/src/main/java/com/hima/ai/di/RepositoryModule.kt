package com.hima.ai.di

import com.hima.ai.data.repository.BackendAiAnalysisRepository
import com.hima.ai.data.repository.SupabaseAuthRepository
import com.hima.ai.domain.repository.AiAnalysisRepository
import com.hima.ai.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAiAnalysisRepository(impl: BackendAiAnalysisRepository): AiAnalysisRepository
}
