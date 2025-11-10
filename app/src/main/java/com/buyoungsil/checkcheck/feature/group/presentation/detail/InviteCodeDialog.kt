package com.buyoungsil.checkcheck.feature.group.presentation.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.buyoungsil.checkcheck.ui.theme.*
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.*

/**
 * 🧡 초대 코드 다이얼로그
 * ✅ 클립보드 복사
 * ✅ 카카오톡 공유 (Feed 템플릿)
 */
@Composable
fun InviteCodeDialog(
    groupName: String,
    inviteCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = ComponentShapes.Dialog,
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "그룹 초대하기",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = TextSecondaryLight
                        )
                    }
                }

                // 그룹 이름
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )

                // 초대 코드 박스
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "초대 코드",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = OrangeBackground,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = OrangePrimary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 4.sp
                        )
                    }
                }

                // 설명 텍스트
                Text(
                    text = "친구에게 코드를 공유하거나\n카카오톡으로 초대해보세요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )

                // 버튼들
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 코드 복사 버튼
                    Button(
                        onClick = {
                            copyToClipboard(context, inviteCode)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "코드 복사",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 카카오톡 공유 버튼
                    OutlinedButton(
                        onClick = {
                            shareToKakao(context, groupName, inviteCode)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFEE500)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "💬",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "카카오톡으로 공유",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }
            }
        }
    }
}

/**
 * 클립보드에 복사
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("초대 코드", text)
    clipboardManager.setPrimaryClip(clip)
    Toast.makeText(context, "초대 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
}

/**
 * 카카오톡으로 공유 (Feed 템플릿)
 */
private fun shareToKakao(context: Context, groupName: String, inviteCode: String) {
    try {
        // ✅ Feed 템플릿 사용 (더 안정적)
        val feedTemplate = FeedTemplate(
            content = Content(
                title = "$groupName 그룹 초대 🎉",
                description = "체크체크 앱에서 함께 습관을 관리해요!\n초대 코드: $inviteCode",
                imageUrl = "https://via.placeholder.com/300x200.png?text=CheckCheck", // 임시 이미지
                link = Link(
                    webUrl = "https://checkcheck.app",
                    mobileWebUrl = "https://checkcheck.app"
                )
            ),
            buttons = listOf(
                Button(
                    title = "앱에서 보기",
                    link = Link(
                        webUrl = "https://checkcheck.app",
                        mobileWebUrl = "https://checkcheck.app"
                    )
                )
            )
        )

        // 카카오톡 설치 확인
        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            // 카카오톡으로 공유
            ShareClient.instance.shareDefault(context, feedTemplate) { sharingResult, error ->
                if (error != null) {
                    android.util.Log.e("KakaoShare", "공유 실패", error)
                    // 실패 시 일반 공유로 폴백
                    shareViaIntent(context, groupName, inviteCode)
                } else if (sharingResult != null) {
                    android.util.Log.d("KakaoShare", "공유 성공: ${sharingResult.intent}")
                    context.startActivity(sharingResult.intent)

                    // 서버 결과 확인 (선택)
                    android.util.Log.w("KakaoShare", "Warning Msg: ${sharingResult.warningMsg}")
                    android.util.Log.w("KakaoShare", "Argument Msg: ${sharingResult.argumentMsg}")
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(feedTemplate)

            try {
                context.startActivity(Intent(Intent.ACTION_VIEW).setData(sharerUrl))
            } catch (e: Exception) {
                android.util.Log.e("KakaoShare", "웹 공유 실패", e)
                shareViaIntent(context, groupName, inviteCode)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("KakaoShare", "카카오 공유 실패", e)
        shareViaIntent(context, groupName, inviteCode)
    }
}

/**
 * 일반 공유 (Fallback)
 */
private fun shareViaIntent(context: Context, groupName: String, inviteCode: String) {
    val shareText = """
        $groupName 그룹에 초대합니다! 🎉
        
        📱 체크체크 앱 설치 후
        초대 코드를 입력해주세요:
        
        $inviteCode
        
        함께 습관을 관리해요!
    """.trimIndent()

    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "초대 코드 공유")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "공유 실패", Toast.LENGTH_SHORT).show()
    }
}