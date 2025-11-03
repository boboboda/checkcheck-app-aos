package com.buyoungsil.checkcheck.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/**
 * 리마인더 설정 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingDialog(
    currentTime: LocalTime?,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime?, Boolean) -> Unit
) {
    var selectedHour by remember { mutableStateOf(currentTime?.hour ?: 9) }
    var selectedMinute by remember { mutableStateOf(currentTime?.minute ?: 0) }
    var isEnabled by remember { mutableStateOf(enabled) }

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
                    Text("알림 받기")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                if (isEnabled) {
                    Divider()

                    // 시간 선택
                    Text(
                        text = "알림 시간",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 시 선택
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("시", style = MaterialTheme.typography.labelSmall)
                            TimePickerWheel(
                                value = selectedHour,
                                range = 0..23,
                                onValueChange = { selectedHour = it }
                            )
                        }

                        Text(":", modifier = Modifier.padding(horizontal = 16.dp))

                        // 분 선택
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("분", style = MaterialTheme.typography.labelSmall)
                            TimePickerWheel(
                                value = selectedMinute,
                                range = 0..59,
                                step = 5, // 5분 단위
                                onValueChange = { selectedMinute = it }
                            )
                        }
                    }

                    // 선택된 시간 표시
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val time = if (isEnabled) {
                        LocalTime.of(selectedHour, selectedMinute)
                    } else null
                    onConfirm(time, isEnabled)
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

/**
 * 시간 선택 휠 (간단 버전)
 */
@Composable
fun TimePickerWheel(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // - 버튼
        FilledTonalIconButton(
            onClick = {
                val newValue = (value - step).coerceIn(range)
                onValueChange(newValue)
            },
            modifier = Modifier.size(40.dp)
        ) {
            Text("-", style = MaterialTheme.typography.titleMedium)
        }

        // 현재 값
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.width(50.dp)
        )

        // + 버튼
        FilledTonalIconButton(
            onClick = {
                val newValue = (value + step).coerceIn(range)
                onValueChange(newValue)
            },
            modifier = Modifier.size(40.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}