package com.buyoungsil.checkcheck.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 할일 알림 설정 다이얼로그
 * ✅ 마감 시간 기준 알림
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskReminderDialog(
    enabled: Boolean,
    minutesBefore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Int) -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var selectedMinutes by remember { mutableStateOf(minutesBefore) }

    val reminderOptions = listOf(
        5 to "5분 전",
        10 to "10분 전",
        30 to "30분 전",
        60 to "1시간 전",
        120 to "2시간 전",
        1440 to "하루 전"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 설정") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 알림 활성화 스위치
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("마감 알림 받기")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                if (isEnabled) {
                    Divider()

                    // 알림 시간 선택
                    Text(
                        text = "알림 시기",
                        style = MaterialTheme.typography.titleSmall
                    )

                    reminderOptions.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMinutes == minutes,
                                onClick = { selectedMinutes = minutes }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(isEnabled, selectedMinutes)
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}