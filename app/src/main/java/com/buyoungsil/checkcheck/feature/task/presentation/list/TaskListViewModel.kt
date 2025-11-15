package com.buyoungsil.checkcheck.feature.task.presentation.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.usecase.ApproveTaskUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.CompleteTaskUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.DeleteTaskUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.GetGroupTasksUseCase
import com.buyoungsil.checkcheck.feature.task.domain.usecase.GetPersonalTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * 태스크 리스트 ViewModel
 *
 * ✅ 책임: 모든 태스크 관련 로직을 하나의 ViewModel에서 처리
 *
 * ## 개인 태스크 vs 그룹 태스크
 * - 개인 태스크: groupId가 빈 문자열 → GetPersonalTasksUseCase 사용
 * - 그룹 태스크: groupId가 있으면 → GetGroupTasksUseCase 사용
 *
 * ## 긴급 태스크 필터링
 * - getUrgentTasks(): 24시간 이내 마감 태스크만 반환
 *
 * @param groupId 빈 문자열이면 개인 태스크, 값이 있으면 그룹 태스크
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getGroupTasksUseCase: GetGroupTasksUseCase,
    private val getPersonalTasksUseCase: GetPersonalTasksUseCase,  // ✅ 개인 태스크 추가
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val approveTaskUseCase: ApproveTaskUseCase,
    savedStateHandle: SavedStateHandle,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "TaskListViewModel"
        private const val URGENT_THRESHOLD_HOURS = 24
    }

    // ✅ groupId가 빈 문자열이면 개인 태스크 모드
    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TaskListViewModel 초기화")
        Log.d(TAG, "  - groupId: ${if (groupId.isEmpty()) "없음 (개인 태스크 모드)" else groupId}")
        Log.d(TAG, "  - userId: $currentUserId")
        Log.d(TAG, "========================================")
        loadTasks()
    }

    /**
     * ✅ 태스크 로드 - 개인/그룹 자동 분기
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                if (groupId.isEmpty()) {
                    Log.d(TAG, "✅ 개인 태스크 로드 시작")
                    loadPersonalTasks()
                } else {
                    Log.d(TAG, "✅ 그룹 태스크 로드 시작 - groupId: $groupId")
                    loadGroupTasks()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 태스크 로드 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "태스크 로드 실패"
                    )
                }
            }
        }
    }

    /**
     * ✅ 개인 태스크 로드
     */
    private suspend fun loadPersonalTasks() {
        getPersonalTasksUseCase(currentUserId).collect { tasks ->
            Log.d(TAG, "✅ 개인 태스크 로드 완료: ${tasks.size}개")

            _uiState.update {
                it.copy(
                    tasks = tasks,
                    isLoading = false,
                    error = null,
                    isPersonalMode = true  // ✅ 개인 모드 플래그
                )
            }
        }
    }

    /**
     * ✅ 그룹 태스크 로드
     */
    private suspend fun loadGroupTasks() {
        getGroupTasksUseCase(groupId).collect { tasks ->
            Log.d(TAG, "✅ 그룹 태스크 로드 완료: ${tasks.size}개")

            _uiState.update {
                it.copy(
                    tasks = tasks,
                    isLoading = false,
                    error = null,
                    isPersonalMode = false,  // ✅ 그룹 모드 플래그
                    currentUserId = currentUserId
                )
            }
        }
    }

    /**
     * ✅ 긴급 태스크 필터 (24시간 이내 마감)
     * HomeScreen의 "긴급 태스크" 섹션에서 사용
     */
    fun getUrgentTasks(): List<Task> {
        val now = LocalDateTime.now()
        val urgentThreshold = now.plusHours(URGENT_THRESHOLD_HOURS.toLong())

        return _uiState.value.tasks.filter { task ->
            // dueDate가 있는 태스크만 필터링
            task.dueDate?.let { dueDate ->
                // dueTime이 있으면 정확한 시간, 없으면 하루 끝 (23:59)으로 계산
                val taskDeadline = if (task.dueTime != null) {
                    LocalDateTime.of(dueDate, task.dueTime)
                } else {
                    LocalDateTime.of(dueDate, LocalTime.of(23, 59))
                }

                // 현재 시간과 긴급 임계값 사이에 있는지 확인
                taskDeadline.isAfter(now) && taskDeadline.isBefore(urgentThreshold)
            } ?: false
        }.sortedBy { task ->
            // 정렬을 위해 LocalDateTime으로 변환
            val dueDate = task.dueDate!!
            val dueTime = task.dueTime ?: LocalTime.of(23, 59)
            LocalDateTime.of(dueDate, dueTime)
        }
    }

    /**
     * ✅ 우선순위별 정렬
     */
    fun getTasksByPriority(): List<Task> {
        return _uiState.value.tasks.sortedByDescending { it.priority.ordinal }
    }

    /**
     * ✅ 개인 태스크만 필터
     */
    fun getPersonalTasksOnly(): List<Task> {
        return _uiState.value.tasks.filter { it.groupId.isEmpty() }
    }

    fun onCompleteTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 완료 처리: $taskId")
            completeTaskUseCase(taskId, currentUserId)
                .onSuccess {
                    Log.d(TAG, "✅ 태스크 완료 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 태스크 완료 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 삭제 처리: $taskId")
            deleteTaskUseCase(taskId)
                .onSuccess {
                    Log.d(TAG, "✅ 태스크 삭제 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 태스크 삭제 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    // ✨ 승인 함수 추가
    fun onApproveTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 승인 처리: $taskId")
            approveTaskUseCase(taskId, currentUserId, approved = true)
                .onSuccess {
                    Log.d(TAG, "✅ 태스크 승인 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 태스크 승인 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    // ✨ 거부 함수 추가
    fun onRejectTask(taskId: String) {
        viewModelScope.launch {
            Log.d(TAG, "태스크 거부 처리: $taskId")
            approveTaskUseCase(taskId, currentUserId, approved = false)
                .onSuccess {
                    Log.d(TAG, "✅ 태스크 거부 성공")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 태스크 거부 실패", error)
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun onRetry() {
        Log.d(TAG, "재시도 - 태스크 다시 로드")
        loadTasks()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}