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
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 그룹 생성 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onCreateGroup: (String, String, String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("family") }
    var selectedIcon by remember { mutableStateOf("👨‍👩‍👧‍👦") }

    val groupTypes = listOf(
        "family" to "가족",
        "couple" to "연인",
        "study" to "스터디",
        "exercise" to "운동",
        "project" to "프로젝트",
        "custom" to "커스텀"
    )

    val iconsByType = mapOf(
        "family" to listOf("👨‍👩‍👧‍👦", "👪", "🏠", "❤️", "🤗"),
        "couple" to listOf("💑", "❤️", "💕", "💖", "💝"),
        "study" to listOf("📚", "📖", "✏️", "🎓", "📝"),
        "exercise" to listOf("🏃", "💪", "🏋️", "🚴", "⚽"),
        "project" to listOf("💼", "📋", "🎯", "⚡", "🚀"),
        "custom" to listOf("🎯", "⭐", "🌟", "✨", "🎨")
    )

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
            GlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "그룹 이름",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    GlassTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = "예: 우리 가족, 스터디 그룹...",
                        singleLine = true
                    )
                }
            }

            // 그룹 타입 선택
            GlassCard {
                Column(
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
                        items(groupTypes) { (type, label) ->
                            TypeChip(
                                label = label,
                                type = type,
                                isSelected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    // 타입 변경 시 기본 아이콘으로 변경
                                    selectedIcon = iconsByType[type]?.firstOrNull() ?: "🎯"
                                }
                            )
                        }
                    }
                }
            }

            // 아이콘 선택
            GlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "그룹 아이콘",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(iconsByType[selectedType] ?: emptyList()) { icon ->
                            IconChip(
                                icon = icon,
                                isSelected = selectedIcon == icon,
                                color = getGroupTypeColor(selectedType),
                                onClick = { selectedIcon = icon }
                            )
                        }
                    }
                }
            }

            // 미리보기
            GlassCard {
                Column(
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
                            containerColor = Color.White
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
                            // 아이콘 배경
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(ComponentShapes.IconBackground)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                getGroupTypeColor(selectedType).copy(alpha = 0.8f),
                                                getGroupTypeColor(selectedType)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedIcon,
                                    fontSize = 28.sp
                                )
                            }

                            Column {
                                Text(
                                    text = groupName.ifEmpty { "그룹 이름" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (groupName.isEmpty()) TextTertiaryLight else TextPrimaryLight
                                )

                                Surface(
                                    shape = ComponentShapes.Badge,
                                    color = getGroupTypeColor(selectedType).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = groupTypes.find { it.first == selectedType }?.second ?: "",
                                        style = CustomTypography.chip,
                                        fontWeight = FontWeight.SemiBold,
                                        color = getGroupTypeColor(selectedType),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 생성 버튼
            GlassButton(
                text = "그룹 만들기",
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName, selectedType, selectedIcon)
                    }
                },
                enabled = groupName.isNotBlank()
            )
        }
    }
}

/**
 * 타입 칩
 */
@Composable
private fun TypeChip(
    label: String,
    type: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = getGroupTypeColor(type)

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
        Text(
            text = label,
            style = CustomTypography.chip,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else TextSecondaryLight,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

/**
 * 아이콘 칩
 */
@Composable
private fun IconChip(
    icon: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(ComponentShapes.IconBackground)
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f),
                            color
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            OrangeSurfaceVariant,
                            OrangeSurfaceVariant
                        )
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 28.sp
        )
    }
}