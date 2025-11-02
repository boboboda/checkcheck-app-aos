package com.buyoungsil.checkcheck.feature.habit.domain.repository

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 습관 Repository 인터페이스
 */
interface HabitRepository {

    // Habit CRUD
    fun getAllHabits(userId: String): Flow<List<Habit>>
    suspend fun getHabitById(habitId: String): Habit?
    fun getPersonalHabits(userId: String): Flow<List<Habit>>
    fun getGroupHabits(groupId: String): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)

    // Habit Check CRUD
    fun getChecksByHabit(habitId: String): Flow<List<HabitCheck>>
    suspend fun getCheckByDate(habitId: String, date: LocalDate): HabitCheck?
    fun getChecksByUserAndDate(userId: String, date: LocalDate): Flow<List<HabitCheck>>
    fun getChecksByDateRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitCheck>>
    suspend fun insertCheck(check: HabitCheck)
    suspend fun deleteCheck(check: HabitCheck)
    suspend fun toggleHabitCheck(habitId: String, userId: String, date: LocalDate)

    // Statistics
    suspend fun getHabitStatistics(habitId: String): HabitStatistics
    suspend fun getCurrentStreak(habitId: String): Int
}