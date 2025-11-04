package com.buyoungsil.checkcheck.core.di

import com.buyoungsil.checkcheck.core.data.repository.UserFirestoreRepository
import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * User Repository DI 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserFirestoreRepository
    ): UserRepository
}