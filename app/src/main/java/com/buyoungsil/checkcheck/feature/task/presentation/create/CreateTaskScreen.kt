package com.buyoungsil.checkcheck.feature.task.presentation.create

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.TaskReminderDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 할일 생성 화면
 * ✅ 알림 설정 UI 추가
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

            // 우선순위 선택
            Text(
                text = "우선순위",
                style = MaterialTheme.typography.titleMedium
            )
            // TODO: 우선순위 선택 UI

            // 마감일 선택
            Text(
                text = "마감일",
                style = MaterialTheme.typography.titleMedium
            )
            // TODO: 날짜/시간 선택 UI

            // ✅ 알림 설정 버튼
            OutlinedButton(
                onClick = {
                    showReminderDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.dueDate != null // 마감일이 있어야 알림 설정 가능
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

            // TODO: 담당자 선택 UI

            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

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

        // ✅ 알림 설정 다이얼로그
        if (showReminderDialog) {
            TaskReminderDialog(
                enabled = uiState.reminderEnabled,
                minutesBefore = uiState.reminderMinutesBefore,
                onDismiss = {
                    showReminderDialog = false
                },
                onConfirm = { enabled, minutes ->
                    // Android 13+ 권한 체크
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