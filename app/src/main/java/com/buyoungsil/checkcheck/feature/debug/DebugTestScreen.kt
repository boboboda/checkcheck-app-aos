package com.buyoungsil.checkcheck.feature.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧪 디버그 전용 테스트 화면
 * BuildConfig.DEBUG에서만 접근 가능
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugTestScreen(
    viewModel: DebugTestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 테스트 결과 메시지 표시
    LaunchedEffect(uiState.testMessage) {
        uiState.testMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearTestMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🧪 디버그 테스트",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = OrangeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangePrimary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ 개발 전용 테스트 화면",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "프로덕션 빌드에서는 접근 불가",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 습관 마일스톤 테스트
            item {
                Text(
                    text = "습관 마일스톤 테스트",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            if (uiState.loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }
            } else if (uiState.habits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text(
                            text = "습관이 없습니다.\n먼저 습관을 생성해주세요.",
                            modifier = Modifier.padding(16.dp),
                            color = TextSecondaryLight
                        )
                    }
                }
            } else {
                items(uiState.habits) { habit ->
                    HabitTestCard(
                        habitTitle = habit.title,
                        currentStreak = uiState.habitStats[habit.id]?.currentStreak ?: 0,
                        lastRewardStreak = habit.lastRewardStreak,
                        onTestMilestone = { days ->
                            viewModel.testMilestone(habit.id, days)
                        },
                        onResetRewards = {
                            viewModel.resetHabitRewards(habit.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitTestCard(
    habitTitle: String,
    currentStreak: Int,
    lastRewardStreak: Int,
    onTestMilestone: (Int) -> Unit,
    onResetRewards: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 습관 정보
            Text(
                text = habitTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "현재 Streak: ${currentStreak}일",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )
                Text(
                    text = "마지막 보상: ${lastRewardStreak}일",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrangePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 펼치기/접기 버튼
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (expanded) "테스트 메뉴 접기 ▲" else "테스트 메뉴 펼치기 ▼",
                    color = OrangePrimary
                )
            }

            // 테스트 버튼들
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "마일스톤 테스트:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 마일스톤 버튼들 (2줄로 배치)
                val milestones = listOf(
                    Triple(3, 2, "3일/2코인"),
                    Triple(7, 5, "7일/5코인"),
                    Triple(14, 10, "14일/10코인"),
                    Triple(21, 20, "21일/20코인"),
                    Triple(30, 50, "30일/50코인"),
                    Triple(50, 100, "50일/100코인"),
                    Triple(100, 200, "100일/200코인")
                )

                milestones.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (days, coins, label) ->
                            Button(
                                onClick = { onTestMilestone(days) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        // 홀수 개일 경우 빈 공간 채우기
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 초기화 버튼
                Button(
                    onClick = onResetRewards,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("⚠️ 보상 기록 초기화")
                }
            }
        }
    }
}