package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.core.util.IconConverter
import com.buyoungsil.checkcheck.feature.coin.domain.model.HabitLimits
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitMilestone
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitMilestones
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 습관 카드
 *
 * ✅ 체크 전용 방식 (토글 제거)
 * - 이미 체크됨 → 클릭 비활성화, 시각적 피드백
 * - 아직 체크 안 됨 → 클릭 가능
 *
 * ✨ 다음 마일스톤 정보 표시
 *
 * @param isCompleted 오늘 체크 완료 여부
 * @param onCheck 체크 클릭 콜백 (이미 체크된 경우 호출되지 않음)
 */
@Composable
fun HabitCard(
    habitName: String,
    isCompleted: Boolean,
    streak: Int = 0,
    completionRate: Float = 0f,
    habitIcon: String = "📝",
    nextMilestoneInfo: NextMilestoneInfo? = null,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .clickable(
                enabled = !isCompleted,  // 🆕 이미 체크된 경우 클릭 비활성화
                onClick = onCheck
            )
            .alpha(if (isCompleted) 0.7f else 1f),  // 🆕 체크된 경우 반투명
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 상단 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽: 아이콘 + 텍스트
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 아이콘
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(ComponentShapes.IconBackground)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isCompleted) {
                                        listOf(
                                            OrangePrimary.copy(alpha = 0.8f),
                                            OrangeSecondary.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        listOf(
                                            OrangePrimary.copy(alpha = 0.15f),
                                            OrangeSecondary.copy(alpha = 0.15f)
                                        )
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayIcon,
                            fontSize = 24.sp
                        )
                    }

                    // 텍스트 정보
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = habitName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryLight
                        )

                        // 스트릭 & 달성률
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (streak > 0) {
                                Text(
                                    text = "🔥 $streak 일 연속",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (completionRate > 0f) {
                                Text(
                                    text = "${(completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                }

                // 오른쪽: 체크박스
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
                                    colors = listOf(
                                        DividerLight,
                                        DividerLight.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 다음 마일스톤 정보 (체크 안 된 경우에만 표시)
            if (nextMilestoneInfo != null && !isCompleted) {
                NextMilestoneInfoCard(info = nextMilestoneInfo)
            }

            // 🆕 체크 완료 메시지
            if (isCompleted) {
                CompletedBadge()
            }
        }
    }
}

/**
 * 🆕 체크 완료 뱃지
 */
@Composable
private fun CompletedBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ComponentShapes.Chip,
        color = OrangePrimary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅ 오늘 완료했습니다!",
                style = MaterialTheme.typography.bodySmall,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 다음 마일스톤 정보 카드
 */
@Composable
private fun NextMilestoneInfoCard(
    info: NextMilestoneInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ComponentShapes.Chip,
        color = OrangePrimary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 진행 정보
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${info.daysLeft}일 더 하면",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    fontSize = 11.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${info.coinsToEarn}코인",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "획득!",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 오른쪽: 진행 바
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${info.currentStreak}/${info.targetDays}일",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    fontSize = 10.sp
                )

                // 프로그레스 바
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        .background(DividerLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(info.progress)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(OrangePrimary, OrangeSecondary)
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * 다음 마일스톤 정보 데이터 클래스
 */
data class NextMilestoneInfo(
    val currentStreak: Int,
    val targetDays: Int,
    val daysLeft: Int,
    val coinsToEarn: Int,
    val progress: Float
) {
    companion object {
        fun fromCurrentStreak(currentStreak: Int): NextMilestoneInfo? {
            val nextMilestone = HabitMilestones.getNextMilestone(currentStreak)
                ?: return null

            val daysLeft = nextMilestone.days - currentStreak
            val progress = currentStreak.toFloat() / nextMilestone.days.toFloat()

            return NextMilestoneInfo(
                currentStreak = currentStreak,
                targetDays = nextMilestone.days,
                daysLeft = daysLeft,
                coinsToEarn = nextMilestone.coins,
                progress = progress.coerceIn(0f, 1f)
            )
        }
    }
}