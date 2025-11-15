package com.buyoungsil.checkcheck.feature.group.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupByIdUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupMembersUseCase  // ✅ 추가
import com.buyoungsil.checkcheck.feature.group.domain.usecase.LeaveGroupUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.UpdateGroupMemberNicknameUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetGroupHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetSharedHabitsInGroupUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.ToggleHabitCheckUseCase
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.usecase.ApproveTaskUseCase
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
    private val getGroupMembersUseCase: GetGroupMembersUseCase,  // ✅ 추가
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val approveTaskUseCase: ApproveTaskUseCase,
    private val updateGroupMemberNicknameUseCase: UpdateGroupMemberNicknameUseCase,
    savedStateHandle: SavedStateHandle,
    private val authManager: FirebaseAuthManager,
    private val getSharedHabitsInGroupUseCase: GetSharedHabitsInGroupUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "GroupDetailViewModel"
    }


    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadGroupDetail()
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentUserId = currentUserId
                )
            }

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

                // ✅ 그룹 습관, 할일, 멤버, 공유 습관 동시에 가져오기
                combine(
                    getGroupHabitsUseCase(groupId),
                    getGroupTasksUseCase(groupId),
                    getGroupMembersUseCase(groupId),
                    getSharedHabitsInGroupUseCase(groupId)  // 🆕 추가
                ) { habits, tasks, members, sharedHabits ->  // 🆕 sharedHabits 추가

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

                    // 🆕 공유 습관에도 통계 추가
                    val sharedHabitsWithStats = sharedHabits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = stats?.currentStreak ?: 0 >= 1

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    // 🆕 멤버별로 그룹화
                    val habitsByMember = sharedHabitsWithStats.groupBy { it.habit.userId }

                    val completedCount = habitsWithStats.count { it.isCheckedToday }
                    val totalCount = habitsWithStats.size

                    // 내 닉네임 찾기
                    val myMember = members.find { it.userId == currentUserId }
                    val myNickname = myMember?.displayName

                    Log.d(TAG, "=== GroupMember 정보 ===")
                    Log.d(TAG, "전체 멤버 수: ${members.size}")
                    Log.d(TAG, "내 닉네임: $myNickname")
                    Log.d(TAG, "공유 습관 수: ${sharedHabits.size}")  // 🆕 로그 추가

                    _uiState.update {
                        it.copy(
                            group = group,
                            sharedHabits = habitsWithStats,
                            tasks = tasks,
                            memberCount = group.memberIds.size,
                            todayCompletedCount = completedCount,
                            todayTotalCount = totalCount,
                            myNickname = myNickname,
                            currentUserId = currentUserId,
                            sharedHabitsByMember = habitsByMember,  // 🆕 추가
                            groupMembers = members,  // 🆕 추가
                            isLoading = false,
                            error = null
                        )
                    }
                }.collect()

            } catch (e: Exception) {
                Log.e(TAG, "❌ 그룹 상세 로드 실패", e)
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

    // ✅ 그룹 나가기 추가
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

    fun onUpdateNickname(newNickname: String) {
        viewModelScope.launch {
            Log.d(TAG, "닉네임 변경 시작: $newNickname")

            updateGroupMemberNicknameUseCase(groupId, currentUserId, newNickname)
                .onSuccess {
                    Log.d(TAG, "✅ 닉네임 변경 성공")
                    // UI 즉시 업데이트
                    _uiState.update { it.copy(myNickname = newNickname) }
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 닉네임 변경 실패: ${error.message}", error)
                    _uiState.update {
                        it.copy(error = error.message ?: "닉네임 변경 실패")
                    }
                }
        }
    }

    // ✨ 승인 함수 추가
    fun onApproveTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 승인: $taskId")
            approveTaskUseCase(taskId, currentUserId, approved = true)
                .onSuccess {
                    Log.d(TAG, "✅ 승인 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 승인 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    // ✨ 거부 함수 추가
    fun onRejectTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 거부: $taskId")
            approveTaskUseCase(taskId, currentUserId, approved = false)
                .onSuccess {
                    Log.d(TAG, "✅ 거부 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 거부 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun onRetry() {
        loadGroupDetail()
    }
}