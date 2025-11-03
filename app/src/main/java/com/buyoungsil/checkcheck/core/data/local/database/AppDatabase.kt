package com.buyoungsil.checkcheck.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.buyoungsil.checkcheck.feature.group.data.local.GroupDao
import com.buyoungsil.checkcheck.feature.group.data.local.GroupEntity
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckEntity
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitEntity
import com.buyoungsil.checkcheck.feature.task.data.local.TaskDao
import com.buyoungsil.checkcheck.feature.task.data.local.TaskEntity

/**
 * 앱 데이터베이스
 * ✅ Task 테이블에 알림 필드 추가 (버전 6)
 */
@Database(
    entities = [
        HabitEntity::class,
        HabitCheckEntity::class,
        GroupEntity::class,
        TaskEntity::class
    ],
    version = 1,  // ✅ 버전 업
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckDao(): HabitCheckDao
    abstract fun groupDao(): GroupDao
    abstract fun taskDao(): TaskDao
}

/**
 * 데이터베이스 마이그레이션
 * Version 5 → 6: Task 테이블에 알림 필드 추가
 */
