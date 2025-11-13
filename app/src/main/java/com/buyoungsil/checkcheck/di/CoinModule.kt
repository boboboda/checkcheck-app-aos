package com.buyoungsil.checkcheck.di

import com.buyoungsil.checkcheck.feature.coin.data.repository.CoinFirestoreRepository
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoinModule {

    @Binds
    @Singleton
    abstract fun bindCoinRepository(
        coinFirestoreRepository: CoinFirestoreRepository
    ): CoinRepository
}