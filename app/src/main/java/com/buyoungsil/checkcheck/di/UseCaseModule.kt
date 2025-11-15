package com.buyoungsil.checkcheck.di

import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.ValidateHabitLimitsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideValidateHabitLimitsUseCase(
        habitRepository: HabitRepository,
        coinRepository: CoinRepository
    ): ValidateHabitLimitsUseCase {
        return ValidateHabitLimitsUseCase(
            habitRepository = habitRepository,
            coinRepository = coinRepository
        )
    }
}