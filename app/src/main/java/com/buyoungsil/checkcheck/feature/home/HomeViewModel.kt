package com.buyoungsil.checkcheck.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.LeaveGroupUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.DeleteHabitUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.ToggleHabitCheckUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.usecase.GetGroupTasksUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.GetPersonalTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val getGroupTasksUseCase: GetGroupTasksUseCase,  // ✅ 추가
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val repository: HabitRepository,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val authManager: FirebaseAuthManager,
    private val getPersonalTasksUseCase: GetPersonalTasksUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "=== HomeViewModel 초기화 시작 ===")
        Log.d(TAG, "currentUserId: $currentUserId")
        loadData()
    }

    private fun loadData() {
        Log.d(TAG, "=== loadData() 시작 ===")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // ✅ 1. 습관 + 그룹 + 개인 할일을 combine으로 동시 로드
                combine(
                    getPersonalHabitsUseCase(currentUserId),
                    getMyGroupsUseCase(currentUserId),
                    repository.getChecksByUserAndDate(currentUserId, LocalDate.now()),
                    getPersonalTasksUseCase(currentUserId)  // ✅ 추가
                ) { habits, groups, todayChecks, personalTasks ->  // ✅ personalTasks 추가

                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = todayChecks.any { it.habitId == habit.id && it.completed }

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    // ✅ Quad로 변경 (4개 반환)
                    QuadData(habitsWithStats, groups, todayChecks.size, personalTasks)
                }
                    .flatMapLatest { quadData ->
                        val (habitsWithStats, groups, todayCompletedCount, personalTasks) = quadData

                        // ✅ 2. 모든 그룹의 할일을 combine으로 실시간 구독
                        if (groups.isEmpty()) {
                            // 그룹이 없으면 빈 리스트 Flow 반환
                            flowOf(QuadData(habitsWithStats, emptyList<Task>(), todayCompletedCount, personalTasks))
                        } else {
                            // 모든 그룹의 할일을 combine으로 합치기
                            combine(
                                groups.map { group ->
                                    getGroupTasksUseCase(group.id)
                                }
                            ) { tasksArrays ->
                                // 모든 그룹의 할일을 하나의 리스트로 합치기
                                val allTasks = tasksArrays.flatMap { it.toList() }

                                // 긴급 필터링
                                val urgentTasks = allTasks.filter { task ->
                                    task.status != TaskStatus.COMPLETED && (
                                            task.priority == TaskPriority.URGENT ||
                                                    task.dueDate?.let { dueDate ->
                                                        dueDate <= LocalDate.now().plusDays(1)
                                                    } == true
                                            )
                                }

                                // 정렬: 우선순위 > 마감일
                                val sortedUrgentTasks = urgentTasks
                                    .sortedWith(
                                        compareBy<Task> { it.priority.ordinal }
                                            .thenBy { it.dueDate ?: LocalDate.MAX }
                                    )

                                Log.d(TAG, "전체 긴급 할일: ${sortedUrgentTasks.size}개")

                                QuadData(habitsWithStats, sortedUrgentTasks, todayCompletedCount, personalTasks)
                            }
                        }
                    }
                    .catch { e ->
                        Log.e(TAG, "❌ 데이터 로드 실패", e)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "데이터 로드 실패"
                            )
                        }
                    }
                    .collect { quadData ->
                        val (habitsWithStats, urgentTasks, todayCompletedCount, personalTasks) = quadData

                        Log.d(TAG, "=== UI 업데이트 ===")
                        Log.d(TAG, "습관: ${habitsWithStats.size}개")
                        Log.d(TAG, "긴급 할일: ${urgentTasks.size}개")
                        Log.d(TAG, "개인 할일: ${personalTasks.size}개")  // ✅ 로그 추가

                        _uiState.update {
                            it.copy(
                                habits = habitsWithStats,
                                urgentTasks = urgentTasks,
                                personalTasks = personalTasks,  // ✅ 추가
                                todayCompletedCount = todayCompletedCount,
                                todayTotalCount = habitsWithStats.size,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ loadData 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "데이터 로드 실패"
                    )
                }
            }
        }
    }

    // ✅ 헬퍼 데이터 클래스 추가 (HomeViewModel 내부 또는 외부)
    data class QuadData<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    operator fun <A, B, C, D> QuadData<A, B, C, D>.component1() = first
    operator fun <A, B, C, D> QuadData<A, B, C, D>.component2() = second
    operator fun <A, B, C, D> QuadData<A, B, C, D>.component3() = third
    operator fun <A, B, C, D> QuadData<A, B, C, D>.component4() = fourth
    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 체크 시작: habitId=$habitId")

            // Optimistic Update
            val currentState = _uiState.value
            val updatedHabits = currentState.habits.map { habitWithStats ->
                if (habitWithStats.habit.id == habitId) {
                    habitWithStats.copy(isCheckedToday = !habitWithStats.isCheckedToday)
                } else {
                    habitWithStats
                }
            }

            val newCompletedCount = updatedHabits.count { it.isCheckedToday }

            _uiState.update {
                it.copy(
                    habits = updatedHabits,
                    todayCompletedCount = newCompletedCount
                )
            }

            // Firestore 업데이트
            try {
                val result = toggleHabitCheckUseCase(
                    habitId = habitId,
                    userId = currentUserId,
                    date = LocalDate.now()
                )

                result.onSuccess {
                    Log.d(TAG, "✅ 습관 체크 성공")
                }.onFailure { error ->
                    Log.e(TAG, "❌ 습관 체크 실패: ${error.message}", error)

                    // 롤백
                    _uiState.update {
                        it.copy(
                            habits = currentState.habits,
                            todayCompletedCount = currentState.todayCompletedCount,
                            error = error.message ?: "습관 체크 실패"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 습관 체크 예외", e)

                // 롤백
                _uiState.update {
                    it.copy(
                        habits = currentState.habits,
                        todayCompletedCount = currentState.todayCompletedCount,
                        error = e.message ?: "습관 체크 중 오류 발생"
                    )
                }
            }
        }
    }

    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 삭제 시작: habitId=$habitId")

            deleteHabitUseCase(habitId)
                .onSuccess {
                    Log.d(TAG, "✅ 습관 삭제 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 습관 삭제 실패: ${error.message}", error)
                    _uiState.update {
                        it.copy(error = error.message ?: "습관 삭제 실패")
                    }
                }
        }
    }

    fun onLeaveGroup(groupId: String) {
        viewModelScope.launch {
            Log.d(TAG, "그룹 탈퇴 시작: groupId=$groupId")

            leaveGroupUseCase(groupId, currentUserId)
                .onSuccess {
                    Log.d(TAG, "✅ 그룹 탈퇴 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 그룹 탈퇴 실패: ${error.message}", error)
                    _uiState.update {
                        it.copy(error = error.message ?: "그룹 탈퇴 실패")
                    }
                }
        }
    }

    fun onRetry() {
        Log.d(TAG, "재시도 버튼 클릭")
        loadData()
    }

}

