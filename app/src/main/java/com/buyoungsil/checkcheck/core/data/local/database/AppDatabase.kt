package com.buyoungsil.checkcheck.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.buyoungsil.checkcheck.feature.group.data.local.GroupDao
import com.buyoungsil.checkcheck.feature.group.data.local.GroupEntity
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckEntity
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitEntity
import com.buyoungsil.checkcheck.feature.task.data.local.TaskDao
import com.buyoungsil.checkcheck.feature.task.data.local.TaskEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCheckEntity::class,
        GroupEntity::class,
        TaskEntity::class  // ← 추가
    ],
    version = 5,  // ← 버전 업
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckDao(): HabitCheckDao
    abstract fun groupDao(): GroupDao
    abstract fun taskDao(): TaskDao  // ← 추가
}