package com.buyoungsil.checkcheck.feature.group.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 개선된 GroupCard - Material Icons 사용
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
            // 그룹 아이콘 (그라데이션 배경) - Material Icon으로 변경
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
                // Material Icon 표시
                Icon(
                    imageVector = getGroupIcon(group.icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = groupColor
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
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * group.icon key에서 Material Icon을 가져오는 함수
 */
private fun getGroupIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        // 생활
        "water_drop" -> Icons.Rounded.WaterDrop
        "notifications" -> Icons.Rounded.Notifications
        "calendar" -> Icons.Rounded.CalendarToday
        "schedule" -> Icons.Rounded.Schedule
        "home" -> Icons.Rounded.Home
        "lightbulb" -> Icons.Rounded.Lightbulb
        "note" -> Icons.Rounded.Note
        "phone" -> Icons.Rounded.Phone
        "yard" -> Icons.Rounded.Yard
        "book" -> Icons.Rounded.Book
        "coffee" -> Icons.Rounded.Coffee
        "eco" -> Icons.Rounded.Eco

        // 건강
        "favorite" -> Icons.Rounded.Favorite
        "monitor_heart" -> Icons.Rounded.MonitorHeart
        "apple" -> Icons.Rounded.LocalDining
        "local_hospital" -> Icons.Rounded.LocalHospital
        "medication" -> Icons.Rounded.Medication
        "hotel" -> Icons.Rounded.Hotel
        "psychology" -> Icons.Rounded.Psychology
        "sentiment" -> Icons.Rounded.SentimentSatisfied
        "visibility" -> Icons.Rounded.Visibility
        "volunteer" -> Icons.Rounded.VolunteerActivism
        "thermostat" -> Icons.Rounded.Thermostat
        "vaccines" -> Icons.Rounded.Vaccines

        // 운동
        "fitness_center" -> Icons.Rounded.FitnessCenter
        "directions_bike" -> Icons.Rounded.DirectionsBike
        "directions_run" -> Icons.Rounded.DirectionsRun
        "directions_walk" -> Icons.Rounded.DirectionsWalk
        "pool" -> Icons.Rounded.Pool
        "self_improvement" -> Icons.Rounded.SelfImprovement
        "basketball" -> Icons.Rounded.SportsBasketball
        "soccer" -> Icons.Rounded.SportsSoccer
        "tennis" -> Icons.Rounded.SportsTennis
        "martial_arts" -> Icons.Rounded.SportsMartialArts
        "sports_score" -> Icons.Rounded.SportsScore
        "timer" -> Icons.Rounded.Timer

        // 공부
        "menu_book" -> Icons.Rounded.MenuBook
        "school" -> Icons.Rounded.School
        "edit" -> Icons.Rounded.Edit
        "create" -> Icons.Rounded.Create
        "backpack" -> Icons.Rounded.Backpack
        "workspace_premium" -> Icons.Rounded.WorkspacePremium
        "calculate" -> Icons.Rounded.Calculate
        "science" -> Icons.Rounded.Science
        "public" -> Icons.Rounded.Public
        "functions" -> Icons.Rounded.Functions
        "biotech" -> Icons.Rounded.Biotech
        "track_changes" -> Icons.Rounded.TrackChanges

        // 취미
        "palette" -> Icons.Rounded.Palette
        "music_note" -> Icons.Rounded.MusicNote
        "piano" -> Icons.Rounded.Piano
        "sports_esports" -> Icons.Rounded.SportsEsports
        "camera_alt" -> Icons.Rounded.CameraAlt
        "movie" -> Icons.Rounded.Movie
        "brush" -> Icons.Rounded.Brush
        "headphones" -> Icons.Rounded.Headphones
        "mic" -> Icons.Rounded.Mic
        "extension" -> Icons.Rounded.Extension
        "celebration" -> Icons.Rounded.Celebration
        "interests" -> Icons.Rounded.Interests

        // 관계
        "groups" -> Icons.Rounded.Groups
        "person" -> Icons.Rounded.Person
        "handshake" -> Icons.Rounded.Handshake
        "forum" -> Icons.Rounded.Forum
        "email" -> Icons.Rounded.Email
        "card_giftcard" -> Icons.Rounded.CardGiftcard
        "emoji_emotions" -> Icons.Rounded.EmojiEmotions
        "waving_hand" -> Icons.Rounded.WavingHand
        "videocam" -> Icons.Rounded.Videocam
        "cake" -> Icons.Rounded.Cake
        "loyalty" -> Icons.Rounded.Loyalty
        "diversity" -> Icons.Rounded.Diversity3

        // 기본값 (이모지 또는 알 수 없는 key)
        else -> Icons.Rounded.Groups // 그룹의 기본 아이콘
    }
}