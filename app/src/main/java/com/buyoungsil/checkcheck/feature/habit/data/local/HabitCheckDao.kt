package com.buyoungsil.checkcheck.feature.habit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 습관 체크 DAO
 * ✅ isCompleted → completed 수정 완료
 */
@Dao
interface HabitCheckDao {

    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId ORDER BY date DESC")
    fun getChecksByHabit(habitId: String): Flow<List<HabitCheckEntity>>

    @Query("SELECT * FROM habit_checks WHERE habitId = :habitId AND date = :date")
    suspend fun getCheckByDate(habitId: String, date: String): HabitCheckEntity?

    @Query("SELECT * FROM habit_checks WHERE userId = :userId AND date = :date")
    fun getChecksByUserAndDate(userId: String, date: String): Flow<List<HabitCheckEntity>>

    @Query("""
        SELECT * FROM habit_checks 
        WHERE habitId = :habitId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC
    """)
    fun getChecksByDateRange(
        habitId: String,
        startDate: String,
        endDate: String
    ): Flow<List<HabitCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: HabitCheckEntity)

    @Delete
    suspend fun deleteCheck(check: HabitCheckEntity)

    @Query("DELETE FROM habit_checks WHERE habitId = :habitId")
    suspend fun deleteChecksByHabit(habitId: String)

    // ✅ isCompleted → completed
    @Query("SELECT COUNT(*) FROM habit_checks WHERE habitId = :habitId AND completed = 1")
    suspend fun getCompletedCount(habitId: String): Int

    // ✅ isCompleted → completed
    @Query("""
        SELECT COUNT(*) 
        FROM habit_checks 
        WHERE habitId = :habitId 
        AND completed = 1 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    suspend fun getCompletedCountInRange(
        habitId: String,
        startDate: String,
        endDate: String
    ): Int
}