package com.buyoungsil.checkcheck.feature.group.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
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
 * 🧡 오렌지 테마 그룹 카드
 * 그룹 타입별 따뜻한 색상 구분
 */
@Composable
fun GroupCard(
    groupName: String,
    groupType: String,
    memberCount: Int,
    completionRate: Float,
    groupIcon: String = "👥",
    isActive: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = getGroupTypeColor(groupType)

    // 활성 상태 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isActive) 4f else 1f,
        animationSpec = spring(),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 상단: 아이콘 + 그룹명 + 타입
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🎨 그라데이션 아이콘 배경
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(ComponentShapes.IconBackground)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        typeColor.copy(alpha = 0.8f),
                                        typeColor
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupIcon,
                            fontSize = 28.sp
                        )
                    }

                    Column {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        // 타입 뱃지
                        Surface(
                            shape = ComponentShapes.Badge,
                            color = typeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = getGroupTypeName(groupType),
                                style = CustomTypography.chip,
                                fontWeight = FontWeight.SemiBold,
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 중단: 멤버 수 + 달성률
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 멤버 수
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "멤버",
                        tint = typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${memberCount}명",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor
                    )
                }

                // 달성률
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = CustomTypography.numberMedium.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    color = getCompletionColor(completionRate * 100)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단: 프로그레스 바
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "오늘의 달성률",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 프로그레스 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(ComponentShapes.ProgressBar)
                        .background(OrangeSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(completionRate)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        typeColor.copy(alpha = 0.8f),
                                        typeColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * 🧡 간단한 그룹 카드
 */
@Composable
fun SimpleGroupCard(
    groupName: String,
    groupType: String,
    memberCount: Int,
    groupIcon: String = "👥",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = getGroupTypeColor(groupType)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 아이콘 배경
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ComponentShapes.IconBackground)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    typeColor.copy(alpha = 0.15f),
                                    typeColor.copy(alpha = 0.25f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = groupIcon,
                        fontSize = 24.sp
                    )
                }

                Column {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Text(
                        text = "${getGroupTypeName(groupType)} · ${memberCount}명",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Surface(
                shape = ComponentShapes.Badge,
                color = typeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = getGroupTypeName(groupType),
                    style = CustomTypography.badge,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 그룹 타입 한글명 반환
 */
private fun getGroupTypeName(type: String): String {
    return when (type.lowercase()) {
        "personal" -> "나만의 공간"  // ✅ 이 줄 추가
        "family" -> "가족"
        "couple" -> "연인"
        "study" -> "스터디"
        "exercise" -> "운동"
        "project" -> "프로젝트"
        else -> "커스텀"
    }
}