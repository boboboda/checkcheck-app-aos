package com.buyoungsil.checkcheck.di

import com.buyoungsil.checkcheck.feature.group.data.repository.GroupFirestoreRepository
import com.buyoungsil.checkcheck.feature.group.data.repository.GroupMemberFirestoreRepository
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupMemberRepository
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import com.buyoungsil.checkcheck.feature.habit.data.repository.HabitFirestoreRepository
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.ranking.data.repository.GlobalRankingFirestoreRepository
import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import com.buyoungsil.checkcheck.feature.task.data.repository.TaskFirestoreRepository
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository DI 모듈
 *
 * 📝 구현체 변경 방법:
 * - Local (Room): HabitRepositoryImpl, GroupRepositoryImpl, TaskRepositoryImpl
 * - Remote (Firebase): HabitFirestoreRepository, GroupFirestoreRepository, TaskFirestoreRepository
 *
 * 현재: Firebase 구현체 사용
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitFirestoreRepository: HabitFirestoreRepository
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupFirestoreRepository: GroupFirestoreRepository
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskFirestoreRepository: TaskFirestoreRepository
    ): TaskRepository

    // ✅ 추가
    @Binds
    @Singleton
    abstract fun bindGroupMemberRepository(
        groupMemberFirestoreRepository: GroupMemberFirestoreRepository
    ): GroupMemberRepository

    @Binds
    @Singleton
    abstract fun bindGlobalRankingRepository(
        impl: GlobalRankingFirestoreRepository
    ): GlobalRankingRepository
}