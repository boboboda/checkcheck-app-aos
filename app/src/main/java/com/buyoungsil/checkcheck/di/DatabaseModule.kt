package com.buyoungsil.checkcheck.di

import android.content.Context
import androidx.room.Room
import com.buyoungsil.checkcheck.core.data.local.database.AppDatabase
import com.buyoungsil.checkcheck.feature.group.data.local.GroupDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitCheckDao
import com.buyoungsil.checkcheck.feature.habit.data.local.HabitDao
import com.buyoungsil.checkcheck.feature.task.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "checkcheck_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitCheckDao(database: AppDatabase): HabitCheckDao {
        return database.habitCheckDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: AppDatabase): GroupDao {
        return database.groupDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
}