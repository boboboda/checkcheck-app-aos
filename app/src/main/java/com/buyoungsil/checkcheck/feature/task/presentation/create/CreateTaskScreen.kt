package com.buyoungsil.checkcheck.feature.task.presentation.create

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 🧡 할일 생성 화면 - 실제 ViewModel에 정확히 맞춤
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "할일 만들기",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight,
                    navigationIconContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 할일 제목
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "할일",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        placeholder = { Text("예: 병원 예약하기") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = uiState.error != null && uiState.title.isBlank()
                    )
                }
            }

            // 설명
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "설명 (선택)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        placeholder = { Text("할일에 대한 설명을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        )
                    )
                }
            }

            // 우선순위 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "우선순위",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedCard(
                        onClick = { showPriorityDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = OrangeSurfaceVariant
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(getPriorityColor(uiState.priority.name.lowercase()))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${getPriorityEmoji(uiState.priority)} ${uiState.priority.displayName}",
                                fontWeight = FontWeight.SemiBold,
                                color = getPriorityColor(uiState.priority.name.lowercase())
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = getPriorityColor(uiState.priority.name.lowercase())
                            )
                        }
                    }
                }
            }

            // 마감일 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "마감일",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (uiState.dueDate != null) OrangeSurfaceVariant else androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.dueDate?.format(DateTimeFormatter.ofPattern("M월 d일"))
                                    ?: "날짜 선택",
                                fontWeight = if (uiState.dueDate != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (uiState.dueDate != null) OrangePrimary else TextSecondaryLight
                            )
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (uiState.dueDate != null) OrangePrimary else TextSecondaryLight
                            )
                        }
                    }
                }
            }

            // 마감 시간 선택 (마감일이 있을 때만)
            if (uiState.dueDate != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.TaskCard,
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "마감 시간 (선택)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        var showTimePicker by remember { mutableStateOf(false) }

                        OutlinedCard(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (uiState.dueTime != null) OrangeSurfaceVariant else androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.dueTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                                        ?: "시간 선택",
                                    fontWeight = if (uiState.dueTime != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (uiState.dueTime != null) OrangePrimary else TextSecondaryLight
                                )
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (uiState.dueTime != null) OrangePrimary else TextSecondaryLight
                                )
                            }
                        }

                        // 시간 피커 다이얼로그
                        if (showTimePicker && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            TimePickerDialog(
                                initialTime = uiState.dueTime ?: LocalTime.of(23, 59),
                                onTimeSelected = { time ->
                                    viewModel.onDueTimeChange(time)
                                    showTimePicker = false
                                },
                                onDismiss = { showTimePicker = false }
                            )
                        }
                    }
                }
            }

// 담당자 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "담당자 (선택)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    var showAssigneeDialog by remember { mutableStateOf(false) }

                    OutlinedCard(
                        onClick = { showAssigneeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (uiState.assigneeName != null) OrangeSurfaceVariant else androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.assigneeName ?: "담당자 지정 안 함 (누구나)",
                                fontWeight = if (uiState.assigneeName != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (uiState.assigneeName != null) OrangePrimary else TextSecondaryLight
                            )
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (uiState.assigneeName != null) OrangePrimary else TextSecondaryLight
                            )
                        }
                    }

                    // 담당자 선택 다이얼로그
                    if (showAssigneeDialog && uiState.selectedGroup != null) {
                        AssigneePickerDialog(
                            group = uiState.selectedGroup!!,
                            currentUserId = viewModel.currentUserId,
                            onAssigneeSelected = { userId, userName ->
                                viewModel.onAssigneeChange(userId, userName)
                                showAssigneeDialog = false
                            },
                            onDismiss = { showAssigneeDialog = false }
                        )
                    }
                }
            }

            // 에러 메시지
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.TaskCard,
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 생성 버튼
            OrangeGradientButton(
                text = if (uiState.loading) "생성 중..." else "할일 만들기",
                onClick = { viewModel.onCreateTask() },
                enabled = !uiState.loading && uiState.title.isNotBlank(),
                icon = Icons.Default.Add
            )
        }
    }

    // 우선순위 선택 다이얼로그
    if (showPriorityDialog) {
        AlertDialog(
            onDismissRequest = { showPriorityDialog = false },
            title = { Text("우선순위 선택") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { priority ->
                        OutlinedCard(
                            onClick = {
                                viewModel.onPriorityChange(priority)
                                showPriorityDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (uiState.priority == priority) {
                                    getPriorityColor(priority.name.lowercase()).copy(alpha = 0.1f)
                                } else {
                                    androidx.compose.ui.graphics.Color.White
                                }
                            )
                        ) {
                            Text(
                                text = "${getPriorityEmoji(priority)} ${priority.displayName}",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = if (uiState.priority == priority) FontWeight.Bold else FontWeight.Normal,
                                color = getPriorityColor(priority.name.lowercase())
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OrangeTextButton(
                    text = "닫기",
                    onClick = { showPriorityDialog = false }
                )
            }
        )
    }

    // DatePicker 다이얼로그
    if (showDatePicker && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                OrangeTextButton(
                    text = "확인",
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDueDateChange(
                                LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            )
                        }
                        showDatePicker = false
                    }
                )
            },
            dismissButton = {
                OrangeTextButton(
                    text = "취소",
                    onClick = { showDatePicker = false }
                )
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = OrangePrimary,
                    todayContentColor = OrangePrimary,
                    todayDateBorderColor = OrangePrimary
                )
            )
        }
    }
}

/**
 * 우선순위 이모지 반환
 */
private fun getPriorityEmoji(priority: TaskPriority): String {
    return when (priority) {
        TaskPriority.URGENT -> "🚨"
        TaskPriority.NORMAL -> "📌"
        TaskPriority.LOW -> "💡"
    }
}

/**
 * 시간 선택 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("시간 선택", fontWeight = FontWeight.Bold) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("확인", color = OrangePrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 담당자 선택 다이얼로그
 */
@Composable
private fun AssigneePickerDialog(
    group: com.buyoungsil.checkcheck.feature.group.domain.model.Group,
    currentUserId: String,
    onAssigneeSelected: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "담당자 선택",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "누구나" 옵션
                OutlinedCard(
                    onClick = {
                        onAssigneeSelected(null, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "담당자 지정 안 함",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "누구나 완료 가능",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = OrangePrimary
                        )
                    }
                }

                // 그룹 멤버들
                if (group.memberIds.isNotEmpty()) {
                    Text(
                        "그룹 멤버",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondaryLight,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    group.memberIds.forEach { memberId ->
                        OutlinedCard(
                            onClick = {
                                // TODO: 실제로는 Firestore에서 사용자 이름을 가져와야 함
                                val memberName = if (memberId == currentUserId) {
                                    "나"
                                } else {
                                    "멤버" // 나중에 실제 이름으로 교체
                                }
                                onAssigneeSelected(memberId, memberName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (memberId == currentUserId) {
                                    OrangeSurfaceVariant
                                } else {
                                    androidx.compose.ui.graphics.Color.White
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (memberId == currentUserId) "나" else "멤버",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (memberId == currentUserId) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                                if (memberId == currentUserId) {
                                    Surface(
                                        shape = ComponentShapes.Chip,
                                        color = OrangePrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "나",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangePrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

