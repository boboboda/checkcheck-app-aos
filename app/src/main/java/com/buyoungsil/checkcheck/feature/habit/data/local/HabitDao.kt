package com.buyoungsil.checkcheck.feature.habit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 습관 DAO
 */
@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHabits(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE userId = :userId AND groupId IS NULL ORDER BY createdAt DESC")
    fun getPersonalHabits(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getGroupHabits(groupId: String): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: String)
}