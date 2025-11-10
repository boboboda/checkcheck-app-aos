package com.buyoungsil.checkcheck.feature.group.presentation.detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.OrangeFAB
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitCard
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskCard
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 그룹 상세 화면 - MZ 오렌지 테마
 * 따뜻하고 활기찬 디자인
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    viewModel: GroupDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHabitCreate: (String) -> Unit,
    onNavigateToTaskCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showOptionsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.group?.name ?: "그룹",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "오늘 ${uiState.todayCompletedCount}/${uiState.todayTotalCount} 완료 🎉",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimaryLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight,
                    navigationIconContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground,
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 할일 추가 버튼
                SmallFloatingActionButton(
                    onClick = onNavigateToTaskCreate,
                    containerColor = OrangeSecondary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "할일 추가",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 습관 추가 메인 버튼
                OrangeFAB(
                    onClick = {
                        uiState.group?.let { onNavigateToHabitCreate(it.id) }
                    },
                    icon = Icons.Default.Add,
                    contentDescription = "습관 추가"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    // 로딩
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = OrangePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "불러오는 중...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    // 에러
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "😢",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = uiState.error ?: "오류가 발생했어요",
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
                    // 정상 - 그룹 상세 내용
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 📊 그룹 정보 카드
                        item {
                            GroupInfoCard(
                                group = uiState.group,
                                memberCount = uiState.memberCount,
                                todayCompletedCount = uiState.todayCompletedCount,
                                todayTotalCount = uiState.todayTotalCount
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
                                    subtitle = "작은 버튼을 눌러 할일을 추가하세요!"
                                )
                            }
                        } else {
                            items(
                                items = uiState.tasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    taskName = task.title,
                                    isCompleted = task.status == TaskStatus.COMPLETED,
                                    priority = task.priority.name.lowercase(),
                                    dueDate = task.dueDate,
                                    assignee = task.assigneeName,
                                    onCheck = { viewModel.onCompleteTask(task.id) }
                                )
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

    // 옵션 메뉴
    DropdownMenu(
        expanded = showOptionsMenu,
        onDismissRequest = { showOptionsMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("그룹 설정") },
            onClick = {
                showOptionsMenu = false
                // TODO: 그룹 설정 화면으로 이동
            },
            leadingIcon = {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("초대 코드 보기") },
            onClick = {
                showOptionsMenu = false
                // TODO: 초대 코드 다이얼로그 표시
            },
            leadingIcon = {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        )
    }
}

/**
 * 📊 그룹 정보 카드
 */
@Composable
private fun GroupInfoCard(
    group: com.buyoungsil.checkcheck.feature.group.domain.model.Group?,
    memberCount: Int,
    todayCompletedCount: Int,
    todayTotalCount: Int
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
            // 그룹 아이콘 & 이름
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 그룹 아이콘
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

                // 그룹 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = group?.name ?: "그룹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "멤버 ${memberCount}명",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                }
            }

            HorizontalDivider(color = DividerLight)

            // 오늘의 진행률
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "오늘의 진행률",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = if (todayTotalCount > 0) {
                            "${(todayCompletedCount.toFloat() / todayTotalCount * 100).toInt()}%"
                        } else {
                            "0%"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }

                // 프로그레스바
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