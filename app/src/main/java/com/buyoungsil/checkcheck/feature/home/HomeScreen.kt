package com.buyoungsil.checkcheck.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListViewModel
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitCard
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 🧡 오렌지 테마 홈 화면
 *
 * ✅ 리팩토링: 각 ViewModel을 직접 주입
 * ✅ 모든 섹션 표시:
 *    - 습관 달성률 통계
 *    - 오늘의 습관
 *    - 긴급 태스크 (TODO)
 *    - 개인 태스크 (TODO)
 *    - 그룹 목록
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    habitViewModel: HabitListViewModel = hiltViewModel(),
    // taskViewModel: TaskListViewModel = hiltViewModel(),  // TODO
    onNavigateToHabitCreate: (String?) -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHabitList: () -> Unit,
    onNavigateToPersonalTaskCreate: () -> Unit,
    onNavigateToCoinWallet: () -> Unit,
    onNavigateToDebug: () -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val habitUiState by habitViewModel.uiState.collectAsState()
    // val taskUiState by taskViewModel.uiState.collectAsState()  // TODO

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
                    // 코인 버튼
                    Surface(
                        modifier = Modifier
                            .clickable { onNavigateToCoinWallet() }
                            .padding(end = 8.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💰", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${homeUiState.totalCoins}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground
                )
            )
        },
        containerColor = OrangeBackground
    ) { paddingValues ->

        // 전체 로딩 상태
        if (homeUiState.isLoading || habitUiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ========== 🆕 습관 달성률 통계 카드 ==========
            item {
                HabitStatisticsCard(habitUiState = habitUiState)
            }

            // ========== 나의 습관 섹션 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💪", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 습관",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onNavigateToHabitList) {
                            Text("전체보기")
                        }
                        IconButton(onClick = { onNavigateToHabitCreate(null) }) {
                            Icon(Icons.Default.Add, contentDescription = "습관 추가")
                        }
                    }
                }
            }

            if (habitUiState.habits.isEmpty()) {
                item {
                    EmptyHabitCard(onNavigateToHabitCreate)
                }
            } else {
                items(
                    items = habitUiState.habits.take(3),
                    key = { it.habit.id }
                ) { habitWithStats ->
                    HabitCard(
                        habitName = habitWithStats.habit.title,
                        isCompleted = habitWithStats.isCheckedToday,
                        streak = habitWithStats.statistics?.currentStreak ?: 0,
                        completionRate = habitWithStats.statistics?.completionRate ?: 0f,
                        habitIcon = habitWithStats.habit.icon,
                        nextMilestoneInfo = habitWithStats.nextMilestoneInfo,
                        onCheck = {
                            habitViewModel.onHabitCheck(habitWithStats.habit.id)
                        }
                    )
                }
            }

            // ========== 🆕 긴급 태스크 섹션 (TODO) ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "긴급 할일",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // TODO: taskViewModel에서 긴급 태스크 표시
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.TaskCard,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "태스크 기능은 TaskListViewModel 구현 후 추가됩니다",
                        modifier = Modifier.padding(16.dp),
                        color = TextSecondaryLight
                    )
                }
            }

            // ========== 🆕 개인 태스크 섹션 (TODO) ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📝", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 할일",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onNavigateToPersonalTaskCreate) {
                        Icon(Icons.Default.Add, contentDescription = "할일 추가")
                    }
                }
            }

            // TODO: taskViewModel에서 개인 태스크 표시

            // ========== 나의 그룹 섹션 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👥", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 그룹",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onNavigateToGroupList) {
                        Text("전체보기")
                    }
                }
            }

            if (homeUiState.groups.isEmpty()) {
                item {
                    EmptyGroupCard(onNavigateToGroupList)
                }
            } else {
                items(
                    items = homeUiState.groups.take(3),
                    key = { it.id }
                ) { group ->
                    GroupItemCard(
                        group = group,
                        onClick = { onNavigateToGroupDetail(group.id) }
                    )
                }
            }
        }
    }
}

/**
 * 🆕 습관 달성률 통계 카드
 */
@Composable
private fun HabitStatisticsCard(
    habitUiState: com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListUiState
) {
    val totalHabits = habitUiState.habits.size
    val completedToday = habitUiState.habits.count { it.isCheckedToday }
    val completionRate = if (totalHabits > 0) {
        (completedToday.toFloat() / totalHabits.toFloat() * 100).toInt()
    } else 0

    val avgStreak = if (totalHabits > 0) {
        habitUiState.habits.mapNotNull { it.statistics?.currentStreak }.average().toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 타이틀
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 오늘의 달성률",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$completionRate%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }

            // 프로그레스 바
            LinearProgressIndicator(
                progress = completionRate / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OrangePrimary,
                trackColor = DividerLight
            )

            // 통계 요약
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    icon = "✅",
                    label = "완료",
                    value = "$completedToday/$totalHabits"
                )

                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = DividerLight
                )

                StatItem(
                    icon = "🔥",
                    label = "평균 연속",
                    value = "${avgStreak}일"
                )
            }
        }
    }
}

/**
 * 통계 아이템
 */
@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
    }
}

/**
 * 빈 습관 카드
 */
@Composable
private fun EmptyHabitCard(
    onNavigateToHabitCreate: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "아직 습관이 없어요",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryLight
            )
            Button(
                onClick = { onNavigateToHabitCreate(null) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("첫 습관 만들기")
            }
        }
    }
}

/**
 * 빈 그룹 카드
 */
@Composable
private fun EmptyGroupCard(
    onNavigateToGroupList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "아직 그룹이 없어요",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryLight
            )
            Button(
                onClick = onNavigateToGroupList,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Icon(Icons.Default.Group, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("그룹 만들기")
            }
        }
    }
}

/**
 * 그룹 카드
 */
@Composable
private fun GroupItemCard(
    group: com.buyoungsil.checkcheck.feature.group.domain.model.Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!group.description.isNullOrEmpty()) {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondaryLight
            )
        }
    }
}

/**
 * 오늘 날짜 포맷
 */
private fun getTodayDate(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    return today.format(formatter)
}