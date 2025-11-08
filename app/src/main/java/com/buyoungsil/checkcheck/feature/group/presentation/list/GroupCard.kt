package com.buyoungsil.checkcheck.feature.group.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 개선된 GroupCard
 * - 그룹 타입별 컬러 적용
 * - 둥글둥글한 디자인
 * - 멤버 수 강조
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 그룹 타입별 컬러
    val groupColor = when (group.type) {
        GroupType.FAMILY -> GroupFamilyColor
        GroupType.COUPLE -> GroupCoupleColor
        GroupType.STUDY -> GroupStudyColor
        GroupType.EXERCISE -> GroupExerciseColor
        GroupType.PROJECT -> GroupProjectColor
        GroupType.CUSTOM -> GroupCustomColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CheckShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 그룹 아이콘 (그라데이션 배경)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                groupColor.copy(alpha = 0.3f),
                                groupColor.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.icon,
                    fontSize = 32.sp
                )
            }

            // 그룹 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 그룹 타입 뱃지
                Surface(
                    shape = CheckShapes.Chip,
                    color = groupColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = group.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = groupColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 그룹 이름
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 멤버 수
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${group.memberIds.size}명",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 최대 인원 표시
                    Text(
                        text = "/ ${group.maxMembers}명",
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckGray500
                    )
                }
            }

            // 나가기 버튼
            IconButton(
                onClick = onLeave,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "그룹 나가기",
                    tint = CheckGray400
                )
            }

            // 화살표
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "상세보기",
                tint = CheckGray400,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}