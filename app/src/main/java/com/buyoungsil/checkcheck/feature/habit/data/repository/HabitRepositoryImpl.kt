package com.buyoungsil.checkcheck.feature.habit.data.repository

import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckEntity
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitEntity
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

/**
 * 습관 Repository 구현
 */
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCheckDao: HabitCheckDao
) : HabitRepository {

    override fun getAllHabits(userId: String): Flow<List<Habit>> {
        return habitDao.getAllHabits(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHabitById(habitId: String): Habit? {
        return habitDao.getHabitById(habitId)?.toDomain()
    }

    override fun getPersonalHabits(userId: String): Flow<List<Habit>> {
        return habitDao.getPersonalHabits(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getGroupHabits(groupId: String): Flow<List<Habit>> {
        return habitDao.getGroupHabits(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun deleteHabit(habitId: String) {
        habitDao.deleteHabitById(habitId)
        habitCheckDao.deleteChecksByHabit(habitId)
    }

    override fun getChecksByHabit(habitId: String): Flow<List<HabitCheck>> {
        return habitCheckDao.getChecksByHabit(habitId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCheckByDate(habitId: String, date: LocalDate): HabitCheck? {
        return habitCheckDao.getCheckByDate(habitId, date.toString())?.toDomain()
    }

    override fun getChecksByUserAndDate(userId: String, date: LocalDate): Flow<List<HabitCheck>> {
        return habitCheckDao.getChecksByUserAndDate(userId, date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChecksByDateRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitCheck>> {
        return habitCheckDao.getChecksByDateRange(
            habitId,
            startDate.toString(),
            endDate.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCheck(check: HabitCheck) {
        habitCheckDao.insertCheck(HabitCheckEntity.fromDomain(check))
    }

    override suspend fun deleteCheck(check: HabitCheck) {
        habitCheckDao.deleteCheck(HabitCheckEntity.fromDomain(check))
    }

    override suspend fun toggleHabitCheck(habitId: String, userId: String, date: LocalDate) {
        val existingCheck = getCheckByDate(habitId, date)

        if (existingCheck != null) {
            deleteCheck(existingCheck)
        } else {
            val newCheck = HabitCheck(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                date = date,
                isCompleted = true
            )
            insertCheck(newCheck)
        }
    }

    override suspend fun getHabitStatistics(habitId: String): HabitStatistics {
        val totalChecks = habitCheckDao.getCompletedCount(habitId)
        val currentStreak = getCurrentStreak(habitId)

        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val monthStart = today.withDayOfMonth(1)

        val thisWeekChecks = habitCheckDao.getCompletedCountInRange(
            habitId,
            weekStart.toString(),
            today.toString()
        )

        val thisMonthChecks = habitCheckDao.getCompletedCountInRange(
            habitId,
            monthStart.toString(),
            today.toString()
        )

        // 생성일부터 오늘까지의 일수로 달성률 계산
        val habit = getHabitById(habitId)
        val daysSinceCreation = if (habit != null) {
            val createdDate = LocalDate.ofEpochDay(habit.createdAt / (24 * 60 * 60 * 1000))
            ChronoUnit.DAYS.between(createdDate, today).toInt() + 1
        } else {
            1
        }

        val completionRate = if (daysSinceCreation > 0) {
            totalChecks.toFloat() / daysSinceCreation
        } else {
            0f
        }

        return HabitStatistics(
            habitId = habitId,
            totalChecks = totalChecks,
            currentStreak = currentStreak,
            longestStreak = currentStreak, // TODO: 별도로 계산 필요
            completionRate = completionRate.coerceIn(0f, 1f),
            thisWeekChecks = thisWeekChecks,
            thisMonthChecks = thisMonthChecks
        )
    }

    override suspend fun getCurrentStreak(habitId: String): Int {
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        // 오늘부터 역순으로 체크하면서 연속 달성일 계산
        while (true) {
            val check = getCheckByDate(habitId, checkDate)
            if (check != null && check.isCompleted) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}