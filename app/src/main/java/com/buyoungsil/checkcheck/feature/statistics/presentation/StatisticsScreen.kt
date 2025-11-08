package com.buyoungsil.checkcheck.feature.statistics.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.statistics.StatisticsViewModel
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

/**
 * 🎨 완전히 새로운 MZ감성 통계 화면
 * - 그라데이션 헤더
 * - 달력 히트맵
 * - 진행률 바 차트
 * - 배지 시스템
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // ✨ 고정된 그라데이션 배경
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ✨ 고정된 상단바
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 통계",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "나의 성장을 확인해보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // ✨ 스크롤 가능한 컨텐츠
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.error ?: "오류",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onRetry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = CheckPrimary
                                )
                            ) {
                                Text("다시 시도")
                            }
                        }
                    }
                }

                uiState.totalHabits == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📊",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "아직 습관이 없어요",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "습관을 만들고 통계를 확인해보세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 100.dp,
                            start = 20.dp,
                            end = 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 🎖️ 배지 섹션
                        item {
                            AchievementBadgesCard(uiState)
                        }

                        // 📊 메인 통계 카드
                        item {
                            MZStatisticsCard(uiState)
                        }

                        // 📅 월간 달력 히트맵
                        item {
                            MonthlyCalendarCard(uiState)
                        }

                        // 📈 기간별 통계
                        item {
                            PeriodStatsCard(uiState)
                        }

                        // 🏆 습관별 순위
                        item {
                            Column {
                                Text(
                                    text = "🏆 습관별 랭킹",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "내 습관들을 스트릭 순으로 정렬했어요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = uiState.habits,
                            key = { it.habit.id }
                        ) { habitWithStats ->
                            HabitRankCard(
                                habitWithStats = habitWithStats,
                                rank = uiState.habits.indexOf(habitWithStats) + 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎖️ 달성 배지 카드
 */
@Composable
fun AchievementBadgesCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🎖️ 달성 배지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BadgeItem(
                    emoji = "🔥",
                    label = "연속 챔피언",
                    achieved = uiState.longestStreak >= 7,
                    value = "${uiState.longestStreak}일"
                )
                BadgeItem(
                    emoji = "💯",
                    label = "완벽주의자",
                    achieved = uiState.averageCompletionRate >= 90f,
                    value = "${uiState.averageCompletionRate.roundToInt()}%"
                )
                BadgeItem(
                    emoji = "⭐",
                    label = "습관 마스터",
                    achieved = uiState.totalChecks >= 100,
                    value = "${uiState.totalChecks}회"
                )
            }
        }
    }
}

@Composable
fun BadgeItem(
    emoji: String,
    label: String,
    achieved: Boolean,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (achieved) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                CheckGray100,
                                CheckGray200
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (achieved) emoji else "🔒",
                fontSize = 32.sp
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (achieved) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (achieved) CheckSuccess else CheckGray400
        )
    }
}

/**
 * 📊 MZ감성 통계 카드
 */
@Composable
fun MZStatisticsCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CheckPrimaryLight.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "전체 통계",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.totalHabits}개 습관 관리 중",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = CheckPrimary
                )
            }

            Divider()

            // 4개 통계를 2x2 그리드로
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        icon = "✅",
                        label = "총 체크",
                        value = "${uiState.totalChecks}",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon = "📈",
                        label = "평균 달성률",
                        value = "${uiState.averageCompletionRate.roundToInt()}%",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        icon = "🔥",
                        label = "최장 스트릭",
                        value = "${uiState.longestStreak}일",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon = "⚡",
                        label = "현재 스트릭",
                        value = "${uiState.currentStreak}일",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CheckPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 📅 월간 달력 히트맵 카드
 */
@Composable
fun MonthlyCalendarCard(uiState: StatisticsUiState) {  // ✅ uiState 파라미터 추가
    val currentMonth = remember { YearMonth.now() }
    val today = remember { LocalDate.now() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                    text = "📅 ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.monthlyCheckDates.size}일 체크",
                    style = MaterialTheme.typography.labelMedium,
                    color = CheckSuccess
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 달력 그리드
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 빈 칸 (월의 시작 전)
                items(startDayOfWeek) {
                    Box(modifier = Modifier.size(36.dp))
                }

                // 실제 날짜들
                items(daysInMonth) { index ->
                    val day = index + 1
                    val date = currentMonth.atDay(day)
                    val isToday = date == today
                    val isChecked = uiState.monthlyCheckDates.contains(date)  // ✅ 실제 데이터 사용

                    CalendarDay(
                        day = day,
                        isToday = isToday,
                        isChecked = isChecked
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isToday: Boolean,
    isChecked: Boolean
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> CheckPrimary
                    isChecked -> CheckSuccess.copy(alpha = 0.3f)
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
                isToday -> Color.White
                isChecked -> CheckSuccess
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * 📈 기간별 통계 카드
 */
@Composable
fun PeriodStatsCard(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📈 기간별 활동",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeriodItem(
                    period = "이번 주",
                    value = "${uiState.thisWeekChecks}",
                    unit = "회",
                    color = CheckPrimary
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                )
                PeriodItem(
                    period = "이번 달",
                    value = "${uiState.thisMonthChecks}",
                    unit = "회",
                    color = CheckSuccess
                )
            }
        }
    }
}

@Composable
fun PeriodItem(
    period: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = period,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
    }
}

/**
 * 🏆 습관별 랭킹 카드
 */
@Composable
fun HabitRankCard(
    habitWithStats: HabitWithStats,
    rank: Int
) {
    val stats = habitWithStats.statistics
    val completionRate = stats?.completionRate ?: 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) {
                when (rank) {
                    1 -> Color(0xFFFFD700).copy(alpha = 0.1f) // 금
                    2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f) // 은
                    3 -> Color(0xFFCD7F32).copy(alpha = 0.1f) // 동
                    else -> Color.White
                }
            } else {
                Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 순위 뱃지
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> CheckGray200
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // 습관 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habitWithStats.habit.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🔥 ${stats?.currentStreak ?: 0}일 연속",
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckOrange
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 진행률 바
                LinearProgressIndicator(
                    progress = { completionRate / 100f },  // 0~100을 0~1로 변환
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> CheckPrimary
                    },
                    trackColor = CheckGray100
                )
            }

            // 달성률
            Text(
                text = "${completionRate.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CheckSuccess
            )
        }
    }
}