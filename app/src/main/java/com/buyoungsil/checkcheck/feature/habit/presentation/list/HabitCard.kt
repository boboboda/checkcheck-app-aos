package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.core.util.IconConverter
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 습관 카드
 * 이모지 렌더링 + 아이콘 변환 문제 해결
 */
@Composable
fun HabitCard(
    habitName: String,
    isCompleted: Boolean,
    streak: Int = 0,
    completionRate: Float = 0f,
    habitIcon: String = "📝",
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ 아이콘 변환 적용
    val displayIcon = IconConverter.convertToEmoji(habitIcon)

    // 체크 시 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 배경색 애니메이션
    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) CheckedBackground else Color.White,
        animationSpec = spring(),
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onCheck),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽: 아이콘 + 텍스트
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🎨 그라데이션 아이콘 배경
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ComponentShapes.IconBackground)
                        .background(
                            Brush.linearGradient(
                                colors = if (isCompleted) {
                                    listOf(OrangePrimary, OrangeSecondary)
                                } else {
                                    listOf(OrangeSurfaceVariant, OrangeBackground)
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ 변환된 이모지 표시
                    Text(
                        text = displayIcon,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                // 습관명 + 스트릭
                Column {
                    Text(
                        text = habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isCompleted) OrangeDark else TextPrimaryLight
                    )

                    if (streak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Default
                            )
                            Text(
                                text = "${streak}일 연속",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 달성률 표시 (스트릭이 없을 때)
                    if (streak == 0 && completionRate > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { completionRate },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp)
                                    .clip(ComponentShapes.Chip),
                                color = OrangePrimary,
                                trackColor = DividerLight,
                            )
                            Text(
                                text = "${(completionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            }

            // 체크박스
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) {
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(UncheckedBackground, UncheckedBackground)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "완료",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 🧡 간단한 습관 카드 (체크만 가능)
 */
@Composable
fun SimpleHabitCard(
    habitName: String,
    isCompleted: Boolean,
    habitIcon: String = "📝",
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ 아이콘 변환 적용
    val displayIcon = IconConverter.convertToEmoji(habitIcon)

    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) CheckedBackground else Color.White,
        animationSpec = spring(),
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onCheck),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) {
                                Brush.linearGradient(
                                    colors = listOf(OrangePrimary, OrangeSecondary)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(OrangeSurfaceVariant, OrangeBackground)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayIcon,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                Text(
                    text = habitName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isCompleted) OrangeDark else TextPrimaryLight
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) {
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(UncheckedBackground, UncheckedBackground)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "완료",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}