package com.buyoungsil.checkcheck.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GetCoinWalletUseCase
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
    private val getCoinWalletUseCase: GetCoinWalletUseCase,
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
                // ✅ 1. 습관 + 그룹 + 개인 할일 + 코인을 combine으로 동시 로드
                combine(
                    getPersonalHabitsUseCase(currentUserId),
                    getMyGroupsUseCase(currentUserId),
                    repository.getChecksByUserAndDate(currentUserId, LocalDate.now()),
                    getPersonalTasksUseCase(currentUserId),
                    getCoinWalletUseCase(currentUserId)  // ✅ 코인 추가
                ) { habits, groups, todayChecks, personalTasks, coinWallet ->

                    // ✅ 코인 로그 추가
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "💰 combine 내부 - 코인 데이터 수신")
                    Log.d(TAG, "coinWallet: $coinWallet")
                    Log.d(TAG, "coinWallet?.totalCoins: ${coinWallet?.totalCoins}")
                    Log.d(TAG, "coinWallet?.familyCoins: ${coinWallet?.familyCoins}")
                    Log.d(TAG, "coinWallet?.rewardCoins: ${coinWallet?.rewardCoins}")
                    Log.d(TAG, "========================================")

                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = todayChecks.any { it.habitId == habit.id && it.completed }

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    // ✅ 코인 정보 추출 (coinWallet은 CoinWallet? 타입)
                    val totalCoins = coinWallet?.totalCoins ?: 0

                    Log.d(TAG, "💰 추출된 totalCoins: $totalCoins")

                    // ✅ QuintData로 변경 (5개 반환)
                    QuintData(habitsWithStats, groups, todayChecks.size, personalTasks, totalCoins)
                }
                    .flatMapLatest { quintData ->
                        val (habitsWithStats, groups, todayCompletedCount, personalTasks, totalCoins) = quintData

                        Log.d(TAG, "💰 flatMapLatest - totalCoins: $totalCoins")

                        // ✅ 2. 모든 그룹의 할일을 combine으로 실시간 구독
                        if (groups.isEmpty()) {
                            // 그룹이 없으면 빈 리스트 Flow 반환
                            flowOf(QuintData(habitsWithStats, emptyList<Task>(), todayCompletedCount, personalTasks, totalCoins))
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
                                Log.d(TAG, "💰 combine 내부 2 - totalCoins: $totalCoins")

                                QuintData(habitsWithStats, sortedUrgentTasks, todayCompletedCount, personalTasks, totalCoins)
                            }
                        }
                    }
                    .catch { e ->
                        Log.e(TAG, "❌ 데이터 로드 실패", e)
                        Log.e(TAG, "❌ 에러 스택트레이스:", e)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "데이터 로드 실패"
                            )
                        }
                    }
                    .collect { quintData ->
                        val (habitsWithStats, urgentTasks, todayCompletedCount, personalTasks, totalCoins) = quintData

                        Log.d(TAG, "=== UI 업데이트 ===")
                        Log.d(TAG, "습관: ${habitsWithStats.size}개")
                        Log.d(TAG, "긴급 할일: ${urgentTasks.size}개")
                        Log.d(TAG, "개인 할일: ${personalTasks.size}개")
                        Log.d(TAG, "💰💰💰 최종 코인: ${totalCoins}개")  // ✅ 강조 로그
                        Log.d(TAG, "========================================")

                        _uiState.update {
                            it.copy(
                                habits = habitsWithStats,
                                urgentTasks = urgentTasks,
                                personalTasks = personalTasks,
                                todayCompletedCount = todayCompletedCount,
                                todayTotalCount = habitsWithStats.size,
                                totalCoins = totalCoins,  // ✅ 코인 업데이트
                                isLoading = false,
                                error = null
                            )
                        }

                        Log.d(TAG, "💰 uiState 업데이트 후 - totalCoins: ${_uiState.value.totalCoins}")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ loadData 실패", e)
                Log.e(TAG, "❌ 에러 상세:", e)
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
    data class QuintData<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    operator fun <A, B, C, D, E> QuintData<A, B, C, D, E>.component1() = first
    operator fun <A, B, C, D, E> QuintData<A, B, C, D, E>.component2() = second
    operator fun <A, B, C, D, E> QuintData<A, B, C, D, E>.component3() = third
    operator fun <A, B, C, D, E> QuintData<A, B, C, D, E>.component4() = fourth
    operator fun <A, B, C, D, E> QuintData<A, B, C, D, E>.component5() = fifth




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

