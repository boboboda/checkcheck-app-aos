package com.buyoungsil.checkcheck.feature.group.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 그룹 생성 화면 - 실제 ViewModel에 정확히 맞춤
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // isSuccess가 true가 되면 뒤로가기
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "그룹 만들기",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
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
        containerColor = OrangeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 그룹명 입력
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "그룹 이름",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        placeholder = { Text("예: 우리 가족, 스터디 그룹...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = uiState.error != null && uiState.name.isBlank()
                    )
                }
            }

            // 그룹명 입력 카드 다음에 추가

// ✅ 닉네임 입력 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "그룹 내 닉네임",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }

                    Text(
                        text = "다른 멤버들에게 이 이름으로 보여요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )

                    OutlinedTextField(
                        value = uiState.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        placeholder = { Text("예: 아빠, 팀장, 리더...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = uiState.error?.contains("닉네임") == true
                    )
                }
            }

            // 그룹 타입 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "그룹 종류",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(GroupType.entries) { type ->
                            TypeChip(
                                type = type,
                                isSelected = uiState.type == type,
                                onClick = { viewModel.onTypeChange(type) }
                            )
                        }
                    }
                }
            }

            // 미리보기
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "미리보기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    // 그룹 카드 미리보기
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ComponentShapes.GroupCard,
                        colors = CardDefaults.cardColors(
                            containerColor = OrangeSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(ComponentShapes.IconBackground)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                getGroupTypeColor(uiState.type.name.lowercase()).copy(alpha = 0.8f),
                                                getGroupTypeColor(uiState.type.name.lowercase())
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.icon,
                                    fontSize = 28.sp
                                )
                            }

                            Column {
                                Text(
                                    text = uiState.name.ifEmpty { "그룹 이름" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.name.isEmpty()) TextTertiaryLight else TextPrimaryLight
                                )

                                Surface(
                                    shape = ComponentShapes.Badge,
                                    color = getGroupTypeColor(uiState.type.name.lowercase()).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = uiState.type.displayName,
                                        style = CustomTypography.chip,
                                        fontWeight = FontWeight.SemiBold,
                                        color = getGroupTypeColor(uiState.type.name.lowercase()),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 에러 메시지
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.GroupCard,
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
                text = if (uiState.isLoading) "생성 중..." else "그룹 만들기",
                onClick = { viewModel.onCreateGroup() },
                enabled = !uiState.isLoading && uiState.name.isNotBlank(),
                icon = Icons.Default.Add
            )
        }
    }
}

/**
 * 타입 칩
 */
@Composable
private fun TypeChip(
    type: GroupType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = getGroupTypeColor(type.name.lowercase())

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = ComponentShapes.Chip,
        color = if (isSelected) {
            color.copy(alpha = 0.15f)
        } else {
            OrangeSurfaceVariant
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type.icon,
                fontSize = 16.sp
            )
            Text(
                text = type.displayName,
                style = CustomTypography.chip,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextSecondaryLight
            )
        }
    }
}