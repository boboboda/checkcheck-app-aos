package com.buyoungsil.checkcheck.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 홈 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHabitCreate: (String?) -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "체크체크",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getTodayDate(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        floatingActionButton = {
            OrangeFAB(
                onClick = { onNavigateToHabitCreate(null) },
                icon = Icons.Default.Add,
                contentDescription = "습관 추가"
            )
        },
        containerColor = OrangeBackground
    ) { padding ->
        when {
            uiState.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = "😢",
                        title = "오류가 발생했어요",
                        subtitle = uiState.error,
                        actionText = "다시 시도",
                        onActionClick = { viewModel.loadData() }
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 100.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 오늘의 요약 카드
                    item {
                        TodaySummaryCard(
                            completedCount = uiState.todayCompletedCount,
                            totalCount = uiState.todayTotalCount,
                            completionRate = uiState.todayCompletionRate
                        )
                    }

                    // 내 습관 섹션
                    item {
                        SectionHeader(
                            title = "📝 내 습관",
                            actionText = if (uiState.personalHabits.isNotEmpty()) "전체보기" else null,
                            onActionClick = if (uiState.personalHabits.isNotEmpty()) {
                                { /* 습관 목록으로 이동 */ }
                            } else null
                        )
                    }

                    if (uiState.personalHabits.isEmpty()) {
                        item {
                            EmptyState(
                                icon = "📭",
                                title = "습관이 없어요",
                                subtitle = "첫 번째 습관을 만들어보세요!",
                                actionText = "습관 추가",
                                onActionClick = { onNavigateToHabitCreate(null) }
                            )
                        }
                    } else {
                        items(
                            items = uiState.personalHabits,
                            key = { it.id }
                        ) { habit ->
                            HabitCard(
                                habitName = habit.name,
                                isCompleted = habit.isCompletedToday,
                                streak = habit.currentStreak,
                                completionRate = habit.completionRate,
                                habitIcon = habit.icon,
                                onCheck = { viewModel.toggleHabitCompletion(habit.id) }
                            )
                        }
                    }

                    // 그룹 섹션
                    if (uiState.groups.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "👥 내 그룹",
                                actionText = "전체보기",
                                onActionClick = onNavigateToGroupList
                            )
                        }

                        items(
                            items = uiState.groups.take(3),
                            key = { it.id }
                        ) { group ->
                            SimpleGroupCard(
                                groupName = group.name,
                                groupType = group.type,
                                memberCount = group.memberIds.size,
                                groupIcon = group.icon,
                                onClick = { onNavigateToGroupDetail(group.id) }
                            )
                        }
                    }

                    // 오늘의 할일 섹션
                    if (uiState.todayTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📋 오늘의 할일",
                                actionText = "전체보기",
                                onActionClick = { /* 할일 목록으로 이동 */ }
                            )
                        }

                        items(
                            items = uiState.todayTasks.take(5),
                            key = { it.id }
                        ) { task ->
                            SimpleTaskCard(
                                taskName = task.title,
                                isCompleted = task.isCompleted,
                                taskIcon = "📋",
                                onCheck = { viewModel.toggleTaskCompletion(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 오늘의 요약 카드
 */
@Composable
private fun TodaySummaryCard(
    completedCount: Int,
    totalCount: Int,
    completionRate: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "오늘의 진행상황",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "화이팅! 조금만 더 힘내요 💪",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                }

                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = CustomTypography.numberLarge,
                    fontWeight = FontWeight.Bold,
                    color = getCompletionColor(completionRate * 100)
                )
            }

            LabeledProgressBar(
                label = "$completedCount / $totalCount 완료",
                progress = completionRate,
                progressColor = getCompletionColor(completionRate * 100)
            )
        }
    }
}

/**
 * 오늘 날짜 반환
 */
private fun getTodayDate(): String {
    val today = java.time.LocalDate.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREAN)
    return today.format(formatter)
}