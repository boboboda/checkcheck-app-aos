package com.buyoungsil.checkcheck.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.LeaveGroupUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.DeleteHabitUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.ToggleHabitCheckUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val repository: HabitRepository,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "=== HomeViewModel 초기화 시작 ===")
        Log.d(TAG, "currentUserId: $currentUserId")
        loadData()
    }

    private fun loadData() {
        Log.d(TAG, "=== loadData() 시작 ===")

        viewModelScope.launch {
            Log.d(TAG, "viewModelScope.launch 진입")
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "isLoading = true 설정 완료")

            try {
                Log.d(TAG, "combine() 호출 전")
                Log.d(TAG, "Flow 1: getPersonalHabitsUseCase($currentUserId)")
                Log.d(TAG, "Flow 2: getMyGroupsUseCase($currentUserId)")
                Log.d(TAG, "Flow 3: getChecksByUserAndDate($currentUserId, ${LocalDate.now()})")

                combine(
                    getPersonalHabitsUseCase(currentUserId)
                        .onStart { Log.d(TAG, "✨ Flow 1 (habits) 시작") }
                        .onEach { Log.d(TAG, "✅ Flow 1 emit: ${it.size}개 습관") },

                    getMyGroupsUseCase(currentUserId)
                        .onStart { Log.d(TAG, "✨ Flow 2 (groups) 시작") }
                        .onEach { Log.d(TAG, "✅ Flow 2 emit: ${it.size}개 그룹") },

                    repository.getChecksByUserAndDate(currentUserId, LocalDate.now())
                        .onStart { Log.d(TAG, "✨ Flow 3 (checks) 시작") }
                        .onEach { Log.d(TAG, "✅ Flow 3 emit: ${it.size}개 체크") }
                ) { habits, groups, todayChecks ->
                    Log.d(TAG, "=== combine 람다 실행 ===")
                    Log.d(TAG, "habits: ${habits.size}개")
                    Log.d(TAG, "groups: ${groups.size}개")
                    Log.d(TAG, "todayChecks: ${todayChecks.size}개")

                    val checkedHabitIds = todayChecks.map { it.habitId }.toSet()

                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = checkedHabitIds.contains(habit.id)

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    val completedCount = habitsWithStats.count { it.isCheckedToday }
                    val totalCount = habitsWithStats.size

                    Log.d(TAG, "통계 포함 습관: ${habitsWithStats.size}개")
                    Log.d(TAG, "오늘 완료: $completedCount / $totalCount")

                    _uiState.update {
                        it.copy(
                            habits = habitsWithStats,
                            groups = groups,
                            todayCompletedCount = completedCount,
                            todayTotalCount = totalCount,
                            isLoading = false,
                            error = null
                        )
                    }

                    Log.d(TAG, "✅ UI State 업데이트 완료 - isLoading=false")
                }.collect()

                Log.d(TAG, "collect() 완료")
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

    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 체크 시작: habitId=$habitId")

            // ✅ 1단계: 즉시 UI 업데이트 (Optimistic Update)
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

            // ✅ 2단계: 백그라운드에서 Firestore 업데이트
            try {
                val result = toggleHabitCheckUseCase(
                    habitId = habitId,
                    userId = currentUserId,
                    date = LocalDate.now()
                )

                result.onSuccess {
                    Log.d(TAG, "✅ 습관 체크 Firestore 동기화 성공: habitId=$habitId")
                    // Flow가 자동으로 업데이트하지만 이미 UI는 변경됨
                }.onFailure { error ->
                    Log.e(TAG, "❌ 습관 체크 실패: ${error.message}", error)

                    // ✅ 3단계: 실패 시 원래 상태로 롤백
                    _uiState.update {
                        it.copy(
                            habits = currentState.habits,
                            todayCompletedCount = currentState.todayCompletedCount,
                            error = error.message ?: "습관 체크 실패"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 습관 체크 예외 발생", e)

                // 실패 시 롤백
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