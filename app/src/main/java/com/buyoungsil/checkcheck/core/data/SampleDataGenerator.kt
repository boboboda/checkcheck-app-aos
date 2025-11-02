package com.buyoungsil.checkcheck.core.data

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataGenerator @Inject constructor(
    private val habitRepository: HabitRepository,
    private val groupRepository: GroupRepository,
    private val taskRepository: TaskRepository
) {
    private val currentUserId = "test_user_id"

    fun generateSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            // 샘플 습관
            val sampleHabits = listOf(
                Habit(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "물 2L 마시기",
                    description = "하루에 물 2리터 마시기",
                    icon = "💧"
                ),
                Habit(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "운동 30분",
                    description = "매일 30분 운동하기",
                    icon = "🏃"
                ),
                Habit(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "독서 20페이지",
                    description = "책 읽기",
                    icon = "📚"
                )
            )

            sampleHabits.forEach { habitRepository.insertHabit(it) }

            // 샘플 그룹
            val sampleGroup = Group(
                id = UUID.randomUUID().toString(),
                name = "우리 가족",
                icon = "👨‍👩‍👧‍👦",
                type = GroupType.FAMILY,
                inviteCode = "FAM123",
                ownerId = currentUserId,
                memberIds = listOf(currentUserId)
            )

            groupRepository.createGroup(sampleGroup)

            // 샘플 할일
            val sampleTasks = listOf(
                Task(
                    id = UUID.randomUUID().toString(),
                    groupId = sampleGroup.id,
                    title = "장보기",
                    description = "우유, 계란, 빵 사오기",
                    priority = TaskPriority.URGENT,
                    createdBy = currentUserId
                ),
                Task(
                    id = UUID.randomUUID().toString(),
                    groupId = sampleGroup.id,
                    title = "청소하기",
                    description = "거실 청소",
                    priority = TaskPriority.NORMAL,
                    createdBy = currentUserId
                )
            )

            sampleTasks.forEach { taskRepository.createTask(it) }
        }
    }
}