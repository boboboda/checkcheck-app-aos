package com.buyoungsil.checkcheck.feature.task.presentation.create

import AssigneePickerDialog
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 🧡 할일 생성 화면
 * ✅ 개인/그룹 구분
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

    // ✅ 개인 할일 여부 확인
    val isPersonalTask = uiState.selectedGroup == null

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
                        // ✅ 개인/그룹 구분
                        text = if (isPersonalTask) "개인 할일 만들기" else "그룹 할일 만들기",
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

            // 알림 설정 (마감 시간이 있을 때만)
            if (uiState.dueTime != null) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "마감 전 알림",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                            Switch(
                                checked = uiState.reminderEnabled,
                                onCheckedChange = { viewModel.onReminderEnabledChange(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    checkedTrackColor = OrangePrimary,
                                    uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    uncheckedTrackColor = TextSecondaryLight
                                )
                            )
                        }

                        if (uiState.reminderEnabled) {
                            var showReminderDialog by remember { mutableStateOf(false) }

                            OutlinedCard(
                                onClick = { showReminderDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = OrangeSurfaceVariant
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
                                        text = getReminderText(uiState.reminderMinutesBefore),
                                        fontWeight = FontWeight.SemiBold,
                                        color = OrangePrimary
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                }
                            }

                            if (showReminderDialog) {
                                ReminderPickerDialog(
                                    currentMinutes = uiState.reminderMinutesBefore,
                                    onMinutesSelected = { minutes ->
                                        viewModel.onReminderMinutesChange(minutes)
                                        showReminderDialog = false
                                    },
                                    onDismiss = { showReminderDialog = false }
                                )
                            }
                        }
                    }
                }
            }

            // 기존 알림 설정 Card 아래에 추가

// 💰 코인 보상 설정
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.TaskCard,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "코인 보상",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = if (uiState.coinReward == 0) "" else uiState.coinReward.toString(),
                        onValueChange = { value ->
                            viewModel.onCoinRewardChanged(value)
                        },
                        label = { Text("완료 시 지급할 코인") },
                        placeholder = { Text("0") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = OrangePrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            focusedLabelColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        )
                    )

                    if (uiState.coinReward > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "이 할일을 완료하면 ${uiState.coinReward}코인을 받을 수 있어요!",
                            fontSize = 14.sp,
                            color = OrangePrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 담당자 선택 - 그룹 할일일 때만 표시
            if (!isPersonalTask) {
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

                        if (showAssigneeDialog) {
                            AssigneePickerDialog(
                                groupMembers = uiState.groupMembers,  // ✅ GroupMember 리스트 전달
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
                onClick = { viewModel.createTask() },
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
 * 알림 시간 텍스트
 */
private fun getReminderText(minutes: Int): String {
    return when (minutes) {
        10 -> "10분 전"
        30 -> "30분 전"
        60 -> "1시간 전"
        120 -> "2시간 전"
        1440 -> "하루 전"
        else -> "${minutes}분 전"
    }
}

/**
 * 알림 시간 선택 다이얼로그
 */
@Composable
private fun ReminderPickerDialog(
    currentMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val reminderOptions = listOf(
        10 to "10분 전",
        30 to "30분 전",
        60 to "1시간 전",
        120 to "2시간 전",
        1440 to "하루 전"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 시간 선택", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reminderOptions.forEach { (minutes, label) ->
                    OutlinedCard(
                        onClick = { onMinutesSelected(minutes) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (minutes == currentMinutes) {
                                OrangeSurfaceVariant
                            } else {
                                androidx.compose.ui.graphics.Color.White
                            }
                        ),
                        border = if (minutes == currentMinutes) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(OrangePrimary)
                            )
                        } else {
                            CardDefaults.outlinedCardBorder()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (minutes == currentMinutes) FontWeight.Bold else FontWeight.Normal,
                                color = if (minutes == currentMinutes) OrangePrimary else TextPrimaryLight
                            )
                            if (minutes == currentMinutes) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = OrangePrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OrangeTextButton(
                text = "닫기",
                onClick = onDismiss
            )
        }
    )
}