package com.buyoungsil.checkcheck.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.CheckCircle
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
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 개선된 HabitCard
 * - MZ감성 귀여운 디자인
 * - 체크 시 만족스러운 애니메이션
 * - 스트릭 불꽃 그라데이션
 * - 둥글둥글한 모서리
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithStats: HabitWithStats,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStats.habit
    val stats = habitWithStats.statistics
    val isChecked = habitWithStats.isCheckedToday

    // 체크 상태에 따른 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "card_scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isChecked) CheckPrimaryLight.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "card_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = CheckShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) {
                CheckPrimaryLight.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        onClick = onCheck
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘 (원형 배경)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecked) {
                            Brush.linearGradient(
                                colors = listOf(
                                    CheckPrimaryLight,
                                    CheckPrimary
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
                    text = habit.icon,
                    fontSize = 28.sp
                )
            }

            // 습관 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) CheckPrimary else MaterialTheme.colorScheme.onSurface
                )

                if (habit.description != null && habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // 통계 정보 (스트릭, 완료 횟수)
                if (stats != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 스트릭 (연속 달성일)
                        if (stats.currentStreak > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${stats.currentStreak}일",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CheckOrange
                                )
                            }
                        }

                        // 총 완료 횟수
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${stats.totalChecks}회",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 완료율
                        if (stats.completionRate > 0) {
                            Text(
                                text = "${(stats.completionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (stats.completionRate >= 0.8f) CheckSuccess else CheckSecondary
                            )
                        }
                    }
                }
            }

            // 체크박스 (애니메이션)
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                IconButton(
                    onClick = onCheck,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isChecked) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
                        contentDescription = if (isChecked) "완료됨" else "미완료",
                        tint = if (isChecked) CheckPrimary else CheckGray400,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 삭제 버튼
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = CheckGray400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}