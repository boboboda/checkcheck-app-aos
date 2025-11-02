package com.buyoungsil.checkcheck.di

import com.buyoungsil.checkcheck.feature.group.data.repository.GroupRepositoryImpl
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import com.buyoungsil.checkcheck.feature.habit.data.repository.HabitRepositoryImpl
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.task.data.repository.TaskRepositoryImpl
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
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
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository


    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}