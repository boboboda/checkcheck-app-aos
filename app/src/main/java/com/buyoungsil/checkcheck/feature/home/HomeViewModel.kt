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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    getPersonalHabitsUseCase(currentUserId),
                    getMyGroupsUseCase(currentUserId),
                    repository.getChecksByUserAndDate(currentUserId, LocalDate.now())
                ) { habits, groups, todayChecks ->
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
                }.collect()
            } catch (e: Exception) {
                Log.e(TAG, "데이터 로드 실패", e)
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
        loadData()
    }
}