package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 코인 선물하기 다이얼로그
 */
@Composable
fun GiftCoinDialog(
    members: List<MemberWithGroup>, // 🆕 타입 변경
    currentUserId: String,
    currentBalance: Int,
    onDismiss: () -> Unit,
    onGift: (toUserId: String, amount: Int, message: String?) -> Unit
) {
    var selectedMember by remember { mutableStateOf<MemberWithGroup?>(null) } // 🆕
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }

    // 자신을 제외한 멤버 목록
    val filteredMembers = remember(members, currentUserId) {
        members.filter { it.userId != currentUserId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 코인 선물하기",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = TextSecondaryLight
                        )
                    }
                }

                // 내 잔액 표시
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "내 잔액",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                        Text(
                            text = "${currentBalance}코인",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                }

                // 멤버 선택 섹션
                Text(
                    text = "📋 멤버 선택",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                // 멤버 목록이 비어있을 때
                if (filteredMembers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = OrangeSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "선물할 수 있는 그룹 멤버가 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // 멤버 목록
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMembers) { member ->
                            MemberItem(
                                member = member,
                                isSelected = selectedMember?.userId == member.userId,
                                onClick = { selectedMember = member }
                            )
                        }
                    }

                    // 코인 수량 입력
                    if (selectedMember != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "코인 수량",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        amount = it
                                        showError = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("선물할 코인 수량") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                isError = showError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = DividerLight
                                )
                            )
                            if (showError != null) {
                                Text(
                                    text = showError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // 메시지 입력
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "메시지 (선택사항)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("응원의 메시지를 남겨보세요") },
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = DividerLight
                                )
                            )
                        }
                    }
                }

                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondaryLight
                        )
                    ) {
                        Text(
                            text = "취소",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 선물하기 버튼
                    Button(
                        onClick = {
                            val amountInt = amount.toIntOrNull()
                            when {
                                amountInt == null || amountInt <= 0 -> {
                                    showError = "1 이상의 코인을 입력하세요"
                                }
                                amountInt > currentBalance -> {
                                    showError = "잔액이 부족합니다"
                                }
                                else -> {
                                    onGift(
                                        selectedMember!!.userId,
                                        amountInt,
                                        message.takeIf { it.isNotBlank() }
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        enabled = selectedMember != null && amount.isNotEmpty()
                    ) {
                        Text(
                            text = "선물하기",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 멤버 아이템
 */
@Composable
private fun MemberItem(
    member: MemberWithGroup, // 🆕 타입 변경
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                OrangePrimary.copy(alpha = 0.15f)
            } else {
                OrangeSurfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 아바타
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(TextSecondaryLight, TextSecondaryLight)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.displayName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 이름 및 그룹 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                // 🆕 그룹 이름 표시
                Text(
                    text = member.groupName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            // 선택 표시
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "선택됨",
                    tint = OrangePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}