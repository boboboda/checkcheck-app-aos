package com.buyoungsil.checkcheck.feature.task.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.notification.TaskReminderScheduler
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupByIdUseCase
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * 할일 생성 ViewModel
 * ✅ 완전 수정: Result 처리 및 알림 스케줄링
 */
@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val taskReminderScheduler: TaskReminderScheduler,
    private val authManager: FirebaseAuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    // ✅ private 제거
    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadGroup()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            // ✅ Result 처리
            getGroupByIdUseCase(groupId)
                .onSuccess { group ->
                    _uiState.update { it.copy(selectedGroup = group) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "그룹 정보를 불러올 수 없습니다")
                    }
                }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onPriorityChange(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onDueDateChange(date: LocalDate?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun onDueTimeChange(time: LocalTime?) {
        _uiState.update { it.copy(dueTime = time) }
    }

    // ✅ 알림 설정 변경
    fun onReminderChange(enabled: Boolean, minutesBefore: Int) {
        _uiState.update {
            it.copy(
                reminderEnabled = enabled,
                reminderMinutesBefore = minutesBefore
            )
        }
    }

    fun onAssigneeChange(assigneeId: String?, assigneeName: String?) {
        _uiState.update {
            it.copy(
                assigneeId = assigneeId,
                assigneeName = assigneeName
            )
        }
    }

    fun onCreateTask() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "할일 제목을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val task = Task(
                id = "",
                groupId = groupId,
                title = currentState.title,
                description = currentState.description.takeIf { it.isNotBlank() },
                assigneeId = currentState.assigneeId,
                assigneeName = currentState.assigneeName,
                status = TaskStatus.PENDING,
                priority = currentState.priority,
                dueDate = currentState.dueDate,
                dueTime = currentState.dueTime,
                reminderEnabled = currentState.reminderEnabled,
                reminderMinutesBefore = currentState.reminderMinutesBefore,
                createdBy = currentUserId
            )

            createTaskUseCase(task)
                .onSuccess { createdTask ->
                    // ✅ 알림 스케줄 설정
                    if (createdTask.reminderEnabled && createdTask.dueDate != null) {
                        val dueDateTime = LocalDateTime.of(
                            createdTask.dueDate,
                            createdTask.dueTime ?: LocalTime.of(23, 59)
                        )

                        val groupName = currentState.selectedGroup?.name ?: "그룹"

                        taskReminderScheduler.scheduleTaskReminder(
                            taskId = createdTask.id,
                            taskTitle = createdTask.title,
                            groupName = groupName,
                            dueDateTime = dueDateTime,
                            minutesBefore = createdTask.reminderMinutesBefore
                        )
                    }

                    _uiState.update {
                        it.copy(
                            loading = false,
                            success = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "할일 생성 실패"
                        )
                    }
                }
        }
    }
}