package com.buyoungsil.checkcheck.feature.task.presentation.create

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.TaskReminderDialog
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 할일 생성 화면
 * ✅ TODO 부분만 완성 (기존 구조 유지)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberNotificationPermissionState()
    var showReminderDialog by remember { mutableStateOf(false) }

    // ✅ TODO 완성을 위한 상태 추가
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("할일 만들기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 할일 제목
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("할일") },
                placeholder = { Text("예: 병원 예약하기") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.title.isBlank()
            )

            // 설명
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("설명 (선택)") },
                placeholder = { Text("할일에 대한 설명을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // ✅ 우선순위 선택 UI 완성
            Text(
                text = "우선순위",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedCard(
                onClick = { showPriorityDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${uiState.priority.icon} ${uiState.priority.displayName}")
                    Icon(Icons.Default.KeyboardArrowDown, null)
                }
            }

            // ✅ 마감일 선택 UI 완성
            Text(
                text = "마감일",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        uiState.dueDate?.format(DateTimeFormatter.ofPattern("M월 d일"))
                            ?: "날짜 선택"
                    )
                    Icon(Icons.Default.CalendarToday, null)
                }
            }

            // ✅ 시간 선택 (마감일이 있을 때만)
            if (uiState.dueDate != null) {
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            uiState.dueTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                                ?: "시간 선택 (선택)"
                        )
                        Icon(Icons.Default.AccessTime, null)
                    }
                }
            }

            // ✅ 알림 설정 버튼
            OutlinedButton(
                onClick = { showReminderDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.dueDate != null
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (uiState.reminderEnabled) {
                        val minutes = uiState.reminderMinutesBefore
                        val time = if (minutes >= 60) "${minutes / 60}시간 전" else "${minutes}분 전"
                        "알림: $time"
                    } else {
                        "알림 설정"
                    }
                )
            }

            // TODO: 담당자 선택 UI (나중에 구현)

            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 생성 버튼
            Button(
                onClick = { viewModel.onCreateTask() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.loading
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("할일 만들기")
                }
            }
        }

        // ✅ 날짜 선택 다이얼로그
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                viewModel.onDueDateChange(date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("취소")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // ✅ 시간 선택 다이얼로그
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            viewModel.onDueTimeChange(time)
                            showTimePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("취소")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }

        // ✅ 우선순위 선택 다이얼로그
        if (showPriorityDialog) {
            AlertDialog(
                onDismissRequest = { showPriorityDialog = false },
                title = { Text("우선순위 선택") },
                text = {
                    Column {
                        TaskPriority.entries.forEach { priority ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = uiState.priority == priority,
                                    onClick = {
                                        viewModel.onPriorityChange(priority)
                                        showPriorityDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${priority.icon} ${priority.displayName}")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // ✅ 알림 설정 다이얼로그
        if (showReminderDialog) {
            TaskReminderDialog(
                enabled = uiState.reminderEnabled,
                minutesBefore = uiState.reminderMinutesBefore,
                onDismiss = { showReminderDialog = false },
                onConfirm = { enabled, minutes ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!permissionState.hasPermission) {
                            permissionState.requestPermission()
                        }
                    }
                    viewModel.onReminderChange(enabled, minutes)
                    showReminderDialog = false
                }
            )
        }
    }
}