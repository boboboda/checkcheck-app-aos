package com.buyoungsil.checkcheck.feature.group.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupByIdUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetGroupHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.ToggleHabitCheckUseCase
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.usecase.CompleteTaskUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.DeleteTaskUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.GetGroupTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getGroupHabitsUseCase: GetGroupHabitsUseCase,
    private val getGroupTasksUseCase: GetGroupTasksUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private val currentUserId = "test_user_id"

    init {
        loadGroupDetail()
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 그룹 정보 가져오기
                val groupResult = getGroupByIdUseCase(groupId)
                val group = groupResult.getOrNull()

                if (group == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "그룹을 찾을 수 없습니다"
                        )
                    }
                    return@launch
                }

                // 그룹 습관과 할일 동시에 가져오기
                combine(
                    getGroupHabitsUseCase(groupId),
                    getGroupTasksUseCase(groupId)
                ) { habits, tasks ->

                    // 습관에 통계 추가
                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = stats?.currentStreak ?: 0 >= 1

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    val completedCount = habitsWithStats.count { it.isCheckedToday }
                    val totalCount = habitsWithStats.size

                    _uiState.update {
                        it.copy(
                            group = group,
                            sharedHabits = habitsWithStats,
                            tasks = tasks,
                            memberCount = group.memberIds.size,
                            todayCompletedCount = completedCount,
                            todayTotalCount = totalCount,
                            isLoading = false,
                            error = null
                        )
                    }
                }.collect()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "데이터 로드 실패"
                    )
                }
            }
        }
    }

    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            toggleHabitCheckUseCase(habitId, currentUserId, LocalDate.now())
            kotlinx.coroutines.delay(100)
            loadGroupDetail()
        }
    }

    fun onCompleteTask(taskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(taskId, currentUserId)
        }
    }

    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    fun onRetry() {
        loadGroupDetail()
    }
}