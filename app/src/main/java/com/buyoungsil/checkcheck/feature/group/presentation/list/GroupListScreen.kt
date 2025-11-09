package com.buyoungsil.checkcheck.feature.group.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
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
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 💜 보라 테마 그룹 목록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    viewModel: GroupListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToJoin: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLeaveDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "내 그룹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 그룹 참여 버튼
                SmallFloatingActionButton(
                    onClick = onNavigateToJoin,
                    containerColor = PurplePrimaryLight,
                    contentColor = Color.White
                ) {
                    Text(
                        "참여",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 그룹 만들기 버튼
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = PurplePrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "그룹 만들기")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PurpleBackground,
                            PurpleSurface
                        )
                    )
                )
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
                            color = PurplePrimary
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
                        GlassButton(
                            text = "다시 시도",
                            onClick = { viewModel.onRetry() }
                        )
                    }
                }

                uiState.groups.isEmpty() -> {
                    // 빈 상태
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 72.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "아직 그룹이 없어요",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "그룹을 만들거나 참여해보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }

                else -> {
                    // 그룹 목록
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.groups,
                            key = { it.id }
                        ) { group ->
                            GroupCard(
                                group = group,
                                onClick = { onNavigateToDetail(group.id) },
                                onLeaveClick = { showLeaveDialog = group.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // 그룹 나가기 확인 다이얼로그
    showLeaveDialog?.let { groupId ->
        AlertDialog(
            onDismissRequest = { showLeaveDialog = null },
            title = {
                Text(
                    "그룹 나가기",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 이 그룹에서 나가시겠어요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLeaveGroup(groupId)
                        showLeaveDialog = null
                    }
                ) {
                    Text(
                        "나가기",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = null }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 그룹 카드
 */
@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                getGroupTypeColor(group.type.name.lowercase()),
                                getGroupTypeColor(group.type.name.lowercase()).copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.icon,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 타입 배지
                    Surface(
                        shape = ComponentShapes.Chip,
                        color = getGroupTypeColor(group.type.name.lowercase()).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = getGroupTypeLabel(group.type.name.lowercase()),
                            style = CustomTypography.chip,
                            fontWeight = FontWeight.SemiBold,
                            color = getGroupTypeColor(group.type.name.lowercase()),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // 멤버 수
                    Text(
                        text = "👥 ${group.memberIds.size}명",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            // 나가기 버튼
            IconButton(
                onClick = onLeaveClick
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "그룹 나가기",
                    tint = TextSecondaryLight
                )
            }
        }
    }
}

/**
 * 그룹 타입별 라벨
 */
private fun getGroupTypeLabel(type: String): String {
    return when (type) {
        "family" -> "가족"
        "couple" -> "연인"
        "study" -> "스터디"
        "exercise" -> "운동"
        "project" -> "프로젝트"
        "custom" -> "커스텀"
        else -> "기타"
    }
}