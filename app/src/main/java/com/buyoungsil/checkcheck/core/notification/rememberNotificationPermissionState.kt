package com.buyoungsil.checkcheck.core.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 알림 권한 요청 Composable
 * ✅ 완전 수정: 로그 추가 및 권한 상태 추적
 */
@Composable
fun rememberNotificationPermissionState(): NotificationPermissionState {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    // ✅ 권한 상태 변경 로깅
    LaunchedEffect(hasPermission) {
        Log.d("NotificationPermission", "권한 상태 변경: $hasPermission")
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("NotificationPermission", "권한 요청 결과: $isGranted")
        hasPermission = isGranted
    }

    return NotificationPermissionState(
        hasPermission = hasPermission,
        requestPermission = {
            Log.d("NotificationPermission", "=== 권한 요청 시작 ===")
            Log.d("NotificationPermission", "Android Version: ${Build.VERSION.SDK_INT}")
            Log.d("NotificationPermission", "TIRAMISU (33): ${Build.VERSION_CODES.TIRAMISU}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d("NotificationPermission", "권한 팝업 표시 시도")
                try {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    Log.e("NotificationPermission", "권한 요청 실패: ${e.message}", e)
                }
            } else {
                Log.d("NotificationPermission", "Android 12 이하, 권한 요청 불필요")
            }
        }
    )
}

data class NotificationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

/**
 * 알림 권한 확인
 */
fun checkNotificationPermission(context: Context): Boolean {
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Android 13 미만은 자동 허용
    }

    Log.d("NotificationPermission", "권한 체크 결과: $result (API ${Build.VERSION.SDK_INT})")
    return result
}