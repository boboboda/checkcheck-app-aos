import android.view.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buyoungsil.checkcheck.ui.theme.ComponentShapes
import com.buyoungsil.checkcheck.ui.theme.OrangePrimary
import com.buyoungsil.checkcheck.ui.theme.OrangeSurfaceVariant
import com.buyoungsil.checkcheck.ui.theme.TextSecondaryLight

/**
 * 담당자 선택 다이얼로그
 * ✅ GroupMember 닉네임 사용
 */
@Composable
fun AssigneePickerDialog(
    groupMembers: List<com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember>,  // ✅ GroupMember 리스트
    currentUserId: String,
    onAssigneeSelected: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "담당자 선택",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "누구나" 옵션
                OutlinedCard(
                    onClick = {
                        onAssigneeSelected(null, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "담당자 지정 안 함",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "누구나 완료 가능",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = OrangePrimary
                        )
                    }
                }

                // ✅ 그룹 멤버들 (GroupMember 사용)
                if (groupMembers.isNotEmpty()) {
                    Text(
                        "그룹 멤버",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondaryLight,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    groupMembers.forEach { member ->
                        OutlinedCard(
                            onClick = {
                                // ✅ GroupMember.displayName 사용 (그룹 닉네임)
                                onAssigneeSelected(member.userId, member.displayName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (member.userId == currentUserId) {
                                    OrangeSurfaceVariant
                                } else {
                                    androidx.compose.ui.graphics.Color.White
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    // ✅ 그룹 닉네임 표시
                                    Text(
                                        member.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (member.userId == currentUserId) {
                                            FontWeight.SemiBold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )

                                    // 역할 표시
                                    if (member.role == com.buyoungsil.checkcheck.feature.group.domain.model.MemberRole.OWNER) {
                                        Text(
                                            "그룹장",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OrangePrimary
                                        )
                                    }
                                }

                                if (member.userId == currentUserId) {
                                    Surface(
                                        shape = ComponentShapes.Chip,
                                        color = OrangePrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "나",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangePrimary,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}