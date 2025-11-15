package com.buyoungsil.checkcheck.feature.group.presentation.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitCard
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskCard
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskApprovalCard  // ✨ 추가
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 그룹 상세 화면
 * ✅ 스피드 다이얼 FAB
 * ✅ 초대 코드 다이얼로그
 * ✅ 그룹 나가기
 * ✅ 승인 프로세스 UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    viewModel: GroupDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHabitCreate: (String) -> Unit,
    onNavigateToTaskCreate: () -> Unit,
    onNavigateToTaskList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showOptionsMenu by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }


    val isOwner = uiState.group?.ownerId == viewModel.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = uiState.group?.name ?: "그룹",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.memberCount}명",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기",
                                tint = Color.Black
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            offset = DpOffset(0.dp, 0.dp)
                        )
                        {
                            DropdownMenuItem(
                                text = { Text("초대하기") },
                                onClick = {
                                    showOptionsMenu = false
                                    showInviteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("내 닉네임 변경") },
                                onClick = {
                                    showOptionsMenu = false
                                    showEditNicknameDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "그룹 나가기",
                                        color = ErrorRed
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showLeaveDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = ErrorRed
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = OrangeBackground,
        floatingActionButton = {
            SpeedDialFAB(
                isExpanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                onHabitClick = {
                    uiState.group?.let { onNavigateToHabitCreate(it.id) }
                    isFabExpanded = false
                },
                onTaskCreateClick = {
                    onNavigateToTaskCreate()
                    isFabExpanded = false
                },
                onTaskListClick = {
                    onNavigateToTaskList()
                    isFabExpanded = false
                }
            )
        }
    )
    { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "오류가 발생했어요",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "다시 시도해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onRetry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("다시 시도")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GroupInfoCard(
                                group = uiState.group,
                                memberCount = uiState.memberCount,
                                todayCompletedCount = uiState.todayCompletedCount,
                                todayTotalCount = uiState.todayTotalCount,
                                isOwner = uiState.group?.ownerId == uiState.currentUserId,
                                myNickname = uiState.myNickname
                            )
                        }

                        // 💪 그룹 습관 섹션
                        item {
                            SectionHeader(
                                icon = "💪",
                                title = "그룹 습관",
                                count = uiState.sharedHabits.size
                            )
                        }

                        if (uiState.sharedHabits.isEmpty()) {
                            item {
                                EmptyCard(
                                    icon = "💪",
                                    title = "아직 공유된 습관이 없어요",
                                    subtitle = "+ 버튼을 눌러 습관을 공유해보세요!"
                                )
                            }
                        } else {
                            items(
                                items = uiState.sharedHabits,
                                key = { it.habit.id }
                            ) { habitWithStats ->
                                HabitCard(
                                    habitName = habitWithStats.habit.title,
                                    isCompleted = habitWithStats.isCheckedToday,
                                    streak = habitWithStats.statistics?.currentStreak ?: 0,
                                    completionRate = habitWithStats.statistics?.completionRate ?: 0f,
                                    habitIcon = habitWithStats.habit.icon,
                                    onCheck = { viewModel.onHabitCheck(habitWithStats.habit.id) }
                                )
                            }
                        }

                        // 🆕 그룹원 습관 섹션
                        if (uiState.sharedHabitsByMember.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionHeader(
                                    icon = "👥",
                                    title = "그룹원 습관",
                                    count = uiState.sharedHabitsByMember.values.sumOf { it.size }
                                )
                            }

                            uiState.sharedHabitsByMember.forEach { (userId, habits) ->
                                item {
                                    val member = uiState.groupMembers.find { it.userId == userId }
                                    val memberName = member?.displayName ?: "알 수 없음"

                                    MemberHabitSection(
                                        memberName = memberName,
                                        habits = habits,
                                        onHabitClick = { /* TODO: 습관 상세 화면 */ }
                                    )
                                }
                            }
                        }

                        // ✅ 그룹 할일 섹션
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(
                                icon = "✅",
                                title = "그룹 할일",
                                count = uiState.tasks.size
                            )
                        }

                        if (uiState.tasks.isEmpty()) {
                            item {
                                EmptyCard(
                                    icon = "✅",
                                    title = "아직 할일이 없어요",
                                    subtitle = "+ 버튼을 눌러 할일을 추가하세요!"
                                )
                            }
                        } else {
                            items(
                                items = uiState.tasks,
                                key = { it.id }
                            ) { task ->
                                when {
                                    // ✅ 승인 대기 상태 & 내가 생성자 → TaskApprovalCard (승인/거부 버튼)
                                    task.status == TaskStatus.WAITING_APPROVAL && task.createdBy == uiState.currentUserId -> {
                                        TaskApprovalCard(
                                            task = task,
                                            onApprove = { viewModel.onApproveTask(task.id) },
                                            onReject = { viewModel.onRejectTask(task.id) }
                                        )
                                    }
                                    // ✅ 승인 대기 상태 & 내가 생성자 아님 → TaskCard (승인 대기 표시만)
                                    task.status == TaskStatus.WAITING_APPROVAL -> {
                                        TaskCard(
                                            taskName = task.title,
                                            isCompleted = false,
                                            status = task.status.name,
                                            priority = task.priority.name.lowercase(),
                                            dueDate = task.dueDate,
                                            dueTime = task.dueTime,
                                            reminderMinutes = task.reminderMinutesBefore,
                                            assignee = task.assigneeName,
                                            createdBy = task.createdBy,
                                            currentUserId = uiState.currentUserId,
                                            onCheck = { }, // 체크 불가
                                            onDelete = null // 삭제 불가
                                        )
                                    }
                                    // ✅ 일반 상태 → TaskCard
                                    else -> {
                                        TaskCard(
                                            taskName = task.title,
                                            isCompleted = task.status == TaskStatus.COMPLETED,
                                            status = task.status.name,
                                            priority = task.priority.name.lowercase(),
                                            dueDate = task.dueDate,
                                            dueTime = task.dueTime,
                                            reminderMinutes = task.reminderMinutesBefore,
                                            assignee = task.assigneeName,
                                            createdBy = task.createdBy,
                                            currentUserId = uiState.currentUserId,
                                            onCheck = { viewModel.onCompleteTask(task.id) },
                                            onDelete = { showDeleteDialog = task.id }
                                        )
                                    }
                                }
                            }
                        }

                        // 하단 여백
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // ✅ 할일 삭제 확인 다이얼로그
    showDeleteDialog?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "할일 삭제",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            },
            text = {
                Text(
                    text = "정말 이 할일을 삭제하시겠어요?\n삭제된 할일은 복구할 수 없습니다.",
                    color = TextSecondaryLight
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteTask(taskId)
                        showDeleteDialog = null
                    }
                ) {
                    Text(
                        text = "삭제",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text(
                        text = "취소",
                        color = TextSecondaryLight
                    )
                }
            },
            containerColor = Color.White,
            shape = ComponentShapes.TaskCard
        )
    }

    if (showEditNicknameDialog) {
        EditNicknameDialog(
            currentNickname = uiState.myNickname,
            onConfirm = { newNickname ->
                viewModel.onUpdateNickname(newNickname)
                showEditNicknameDialog = false
            },
            onDismiss = { showEditNicknameDialog = false }
        )
    }

    if (showInviteDialog && uiState.group != null) {
        InviteCodeDialog(
            groupName = uiState.group!!.name,
            inviteCode = uiState.group!!.inviteCode,
            onDismiss = { showInviteDialog = false }
        )
    }

    if (showLeaveDialog && uiState.group != null) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text(
                    "그룹 나가기",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("'${uiState.group!!.name}' 그룹에서 나가시겠어요?\n공유 습관과 할일을 더 이상 볼 수 없어요.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onLeaveGroup(uiState.group!!.id)
                        showLeaveDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("나가기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * ✨ 스피드 다이얼 FAB (수정됨)
 */
@Composable
private fun SpeedDialFAB(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onHabitClick: () -> Unit,
    onTaskCreateClick: () -> Unit,
    onTaskListClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 할일 목록 보기
                SpeedDialItem(
                    icon = Icons.Default.List,
                    label = "할일 목록",
                    onClick = onTaskListClick,
                    backgroundColor = Color(0xFF4CAF50)
                )

                // 습관 추가
                SpeedDialItem(
                    icon = Icons.Default.CheckCircle,
                    label = "습관 추가",
                    onClick = onHabitClick,
                    backgroundColor = OrangePrimary
                )

                // 할일 추가
                SpeedDialItem(
                    icon = Icons.Default.Add,
                    label = "할일 추가",
                    onClick = onTaskCreateClick,
                    backgroundColor = OrangeSecondary
                )
            }
        }

        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 45f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "rotation"
        )

        FloatingActionButton(
            onClick = { onExpandedChange(!isExpanded) },
            modifier = Modifier.size(64.dp),
            shape = ComponentShapes.FloatingButton,
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OrangePrimary, OrangeSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "닫기" else "메뉴",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 스피드 다이얼 아이템
 */
@Composable
private fun SpeedDialItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = ComponentShapes.Chip,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = backgroundColor,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun GroupInfoCard(
    group: com.buyoungsil.checkcheck.feature.group.domain.model.Group?,
    memberCount: Int,
    todayCompletedCount: Int,
    todayTotalCount: Int,
    isOwner: Boolean,
    myNickname: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    getGroupTypeColor(group?.type?.name?.lowercase() ?: ""),
                                    getGroupTypeColor(group?.type?.name?.lowercase() ?: "").copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group?.icon ?: "👥",
                        fontSize = 32.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = group?.name ?: "그룹",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        if (myNickname != null) {
                            Text(
                                text = "($myNickname)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangePrimary
                            )
                        }

                        Surface(
                            shape = ComponentShapes.Badge,
                            color = if (isOwner) {
                                OrangePrimary.copy(alpha = 0.15f)
                            } else {
                                Color.Gray.copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isOwner) "👑" else "👤",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isOwner) "그룹장" else "멤버",
                                    style = CustomTypography.chip,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOwner) OrangePrimary else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = ComponentShapes.Chip,
                            color = getGroupTypeColor(group?.type?.name?.lowercase() ?: "").copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = group?.type?.displayName ?: "",
                                style = CustomTypography.chip,
                                fontWeight = FontWeight.SemiBold,
                                color = getGroupTypeColor(group?.type?.name?.lowercase() ?: ""),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "•",
                            color = TextSecondaryLight
                        )

                        Text(
                            text = "👥 ${memberCount}명",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            Divider(color = DividerLight)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "오늘의 달성 현황",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )

                LinearProgressIndicator(
                    progress = {
                        if (todayTotalCount > 0) {
                            todayCompletedCount.toFloat() / todayTotalCount
                        } else {
                            0f
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(ComponentShapes.ProgressBar),
                    color = OrangePrimary,
                    trackColor = OrangeSurfaceVariant,
                )

                Text(
                    text = "$todayCompletedCount / $todayTotalCount 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

/**
 * 📌 섹션 헤더
 */
@Composable
private fun SectionHeader(
    icon: String,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondaryLight
        )
    }
}

/**
 * 📭 빈 카드
 */
@Composable
private fun EmptyCard(
    icon: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * 🆕 멤버별 습관 섹션
 */
@Composable
private fun MemberHabitSection(
    memberName: String,
    habits: List<com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats>,
    onHabitClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangeSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = memberName.take(1),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
                Text(
                    text = "${habits.size}개",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )
            }

            Divider(color = DividerLight, thickness = 1.dp)

            habits.forEach { habitWithStats ->
                MemberHabitItem(
                    habitWithStats = habitWithStats,
                    onClick = { onHabitClick(habitWithStats.habit.id) }
                )
            }
        }
    }
}

/**
 * 🆕 멤버 습관 아이템
 */
@Composable
private fun MemberHabitItem(
    habitWithStats: com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = if (habitWithStats.isCheckedToday) {
                OrangeSurfaceVariant
            } else {
                OrangeBackground
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (habitWithStats.isCheckedToday) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = if (habitWithStats.isCheckedToday) {
                        OrangePrimary
                    } else {
                        TextSecondaryLight
                    },
                    modifier = Modifier.size(24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = habitWithStats.habit.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "🔥 ${habitWithStats.statistics?.currentStreak ?: 0}일 연속",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            habitWithStats.statistics?.let { stats ->
                Text(
                    text = "${stats.completionRate}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }
        }
    }
}