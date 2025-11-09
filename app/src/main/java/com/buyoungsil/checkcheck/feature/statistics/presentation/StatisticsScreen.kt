package com.buyoungsil.checkcheck.feature.statistics.presentation

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.statistics.StatisticsViewModel
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * 🧡 오렌지 테마 통계 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "📊 통계",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "나의 성장을 확인해보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground
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
                        CircularProgressIndicator(
                            color = OrangePrimary
                        )
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
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "오류가 발생했어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onRetry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }

                uiState.totalHabits == 0 -> {
                    // 빈 상태
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 72.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "아직 습관이 없어요",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "습관을 만들고 통계를 확인해보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }

                else -> {
                    // 통계 콘텐츠
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 전체 통계 카드
                        item {
                            OverallStatsCard(uiState)
                        }

                        // 월간 달력 히트맵
                        item {
                            MonthlyCalendarCard(uiState)
                        }

                        // 기간별 통계
                        item {
                            PeriodStatsCard(uiState)
                        }

                        // 배지 섹션
                        item {
                            AchievementBadgesCard(uiState)
                        }

                        // 습관별 랭킹
                        item {
                            Text(
                                text = "🏆 습관별 랭킹",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }

                        items(
                            items = uiState.habits.sortedByDescending {
                                it.statistics?.currentStreak ?: 0
                            },
                            key = { it.habit.id }
                        ) { habitWithStats ->
                            HabitRankCard(habitWithStats)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 전체 통계 카드
 */
@Composable
private fun OverallStatsCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "전체 통계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "총 습관",
                    value = "${uiState.totalHabits}개"
                )
                StatItem(
                    label = "총 체크",
                    value = "${uiState.totalChecks}회"
                )
                StatItem(
                    label = "평균 달성률",
                    value = "${uiState.averageCompletionRate.toInt()}%"
                )
            }

            HorizontalDivider(color = DividerLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "최장 스트릭",
                    value = "${uiState.longestStreak}일",
                    icon = "🔥"
                )
                StatItem(
                    label = "현재 스트릭",
                    value = "${uiState.currentStreak}일",
                    icon = "⚡"
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
    label: String,
    value: String,
    icon: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OrangePrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )
    }
}

/**
 * 월간 달력 히트맵 카드
 */
@Composable
private fun MonthlyCalendarCard(uiState: StatisticsUiState) {
    val currentMonth = remember { YearMonth.now() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "${uiState.monthlyCheckDates.size}일 체크",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 요일 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 달력 그리드
            CalendarGrid(
                currentMonth = currentMonth,
                checkedDates = uiState.monthlyCheckDates
            )
        }
    }
}

/**
 * 달력 그리드
 */
@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    checkedDates: Set<LocalDate>
) {
    val firstDay = currentMonth.atDay(1)
    val lastDay = currentMonth.atEndOfMonth()
    val daysInMonth = currentMonth.lengthOfMonth()
    val startDayOfWeek = firstDay.dayOfWeek.value % 7

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var currentDay = 1
        var weekIndex = 0

        while (currentDay <= daysInMonth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if (weekIndex == 0 && dayOfWeek < startDayOfWeek) {
                        // 빈 칸
                        Spacer(modifier = Modifier.weight(1f))
                    } else if (currentDay <= daysInMonth) {
                        val date = currentMonth.atDay(currentDay)
                        val isChecked = checkedDates.contains(date)
                        val isToday = date == LocalDate.now()

                        DayCell(
                            day = currentDay,
                            isChecked = isChecked,
                            isToday = isToday,
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            weekIndex++
        }
    }
}

/**
 * 날짜 셀
 */
@Composable
private fun DayCell(
    day: Int,
    isChecked: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isChecked -> OrangePrimary
                    isToday -> OrangeLight
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isChecked -> Color.White
                isToday -> OrangePrimary
                else -> TextPrimaryLight
            }
        )
    }
}

/**
 * 기간별 통계 카드
 */
@Composable
private fun PeriodStatsCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📈 기간별 통계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            PeriodStatRow(
                label = "이번 주",
                value = uiState.thisWeekChecks,
                total = uiState.totalHabits * 7
            )

            PeriodStatRow(
                label = "이번 달",
                value = uiState.thisMonthChecks,
                total = uiState.totalHabits * LocalDate.now().lengthOfMonth()
            )
        }
    }
}

/**
 * 기간 통계 행
 */
@Composable
private fun PeriodStatRow(
    label: String,
    value: Int,
    total: Int
) {
    val progress = if (total > 0) value.toFloat() / total else 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryLight
            )
            Text(
                text = "$value / $total",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(ComponentShapes.Chip),
            color = OrangePrimary,
            trackColor = DividerLight,
        )
    }
}

/**
 * 배지 카드
 */
@Composable
private fun AchievementBadgesCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎖️ 달성 배지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BadgeItem(
                    emoji = "🔥",
                    label = "7일 연속",
                    achieved = uiState.longestStreak >= 7
                )
                BadgeItem(
                    emoji = "💪",
                    label = "30일 연속",
                    achieved = uiState.longestStreak >= 30
                )
                BadgeItem(
                    emoji = "👑",
                    label = "100회 달성",
                    achieved = uiState.totalChecks >= 100
                )
            }
        }
    }
}

/**
 * 배지 아이템
 */
@Composable
private fun BadgeItem(
    emoji: String,
    label: String,
    achieved: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (achieved) {
                        Brush.linearGradient(
                            colors = listOf(
                                OrangePrimary,
                                OrangeSecondary
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                DividerLight,
                                DividerLight
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (achieved) emoji else "🔒",
                fontSize = 28.sp
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (achieved) TextPrimaryLight else TextSecondaryLight
        )
    }
}

/**
 * 습관 랭킹 카드
 */
@Composable
private fun HabitRankCard(habitWithStats: HabitWithStats) {
    val habit = habitWithStats.habit
    val stats = habitWithStats.statistics

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ComponentShapes.IconBackground)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                OrangePrimary,
                                OrangeSecondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                if (stats != null) {
                    Text(
                        text = "달성률 ${stats.completionRate.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            // 스트릭
            if (stats != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "🔥 ${stats.currentStreak}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Text(
                        text = "${stats.totalChecks}회",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}