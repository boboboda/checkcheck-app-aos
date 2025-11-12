package com.buyoungsil.checkcheck.feature.group.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buyoungsil.checkcheck.ui.theme.ComponentShapes
import com.buyoungsil.checkcheck.ui.theme.DividerLight
import com.buyoungsil.checkcheck.ui.theme.OrangePrimary
import com.buyoungsil.checkcheck.ui.theme.TextSecondaryLight

/**
 * 닉네임 변경 다이얼로그
 */
@Composable
fun EditNicknameDialog(
    currentNickname: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "그룹 닉네임 변경",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "이 그룹에서 사용할 닉네임을 입력하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = { Text("예: 아빠, 팀장, 리더...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = ComponentShapes.TextField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = DividerLight,
                        cursorColor = OrangePrimary,
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nickname.isNotBlank()) {
                        onConfirm(nickname)
                    }
                },
                enabled = nickname.isNotBlank()
            ) {
                Text(
                    "변경",
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        containerColor = Color.White,
        shape = ComponentShapes.Dialog
    )
}