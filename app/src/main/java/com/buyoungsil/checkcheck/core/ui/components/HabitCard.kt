package com.buyoungsil.checkcheck.core.ui.components

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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 습관 카드
 * 따뜻하고 친근한 느낌의 디자인
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

    // 테두리 색상 애니메이션
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) OrangePrimary else Color(0xFFEEEEEE),
        animationSpec = spring(),
        label = "borderColor"
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
                    Text(
                        text = habitIcon,
                        fontSize = 24.sp
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
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${streak}일 연속",
                                style = MaterialTheme.typography.bodySmall,
                                color = getStreakColor(streak),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 오른쪽: 체크박스 + 달성률
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 달성률 표시
                if (completionRate > 0f) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${(completionRate * 100).toInt()}%",
                            style = CustomTypography.numberSmall,
                            color = getCompletionColor(completionRate * 100)
                        )

                        // 작은 프로그레스 바
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(ComponentShapes.ProgressBar)
                                .background(OrangeSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(completionRate)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(OrangePrimary, OrangeSecondary)
                                        )
                                    )
                            )
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
            defaultElevation = if (isCompleted) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = habitIcon,
                    fontSize = 20.sp
                )

                Text(
                    text = habitName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCompleted) OrangeDark else TextPrimaryLight
                )
            }

            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Circle,
                contentDescription = if (isCompleted) "완료" else "미완료",
                tint = if (isCompleted) OrangePrimary else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}