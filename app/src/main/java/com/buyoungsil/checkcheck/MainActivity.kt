package com.buyoungsil.checkcheck

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.ui.navigation.NavGraph
import com.buyoungsil.checkcheck.core.ui.navigation.Screen
import com.buyoungsil.checkcheck.ui.theme.CheckcheckTheme
import com.google.firebase.messaging.FirebaseMessaging  // ✅ FCM 추가
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Firebase 익명 로그인
        lifecycleScope.launch {
            if (authManager.currentUser == null) {
                Log.d(TAG, "⏳ Firebase 익명 로그인 시도...")
                val result = authManager.signInAnonymously()
                result.onSuccess { user ->
                    Log.d(TAG, "✅ Firebase 익명 로그인 성공")
                    Log.d(TAG, "   User ID: ${user.uid}")
                }.onFailure { error ->
                    Log.e(TAG, "❌ Firebase 로그인 실패: ${error.message}")
                }
            } else {
                Log.d(TAG, "✅ 이미 로그인됨: ${authManager.currentUser?.uid}")
            }
        }

        // ✅ FCM 토큰 확인
        checkFcmToken()

        enableEdgeToEdge()
        setContent {
            CheckcheckTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 로그인 상태 확인
                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                if (authState == null) {
                    // 로딩 화면
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("로그인 중...")
                        }
                    }
                } else {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute in listOf(
                                    Screen.Home.route,
                                    Screen.GroupList.route,
                                    Screen.Statistics.route
                                )) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, "홈") },
                                        label = { Text("홈") },
                                        selected = currentRoute == Screen.Home.route,
                                        onClick = {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Home.route) { inclusive = true }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.People, "그룹") },
                                        label = { Text("그룹") },
                                        selected = currentRoute == Screen.GroupList.route,
                                        onClick = {
                                            navController.navigate(Screen.GroupList.route) {
                                                popUpTo(Screen.Home.route)
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.BarChart, "통계") },
                                        label = { Text("통계") },
                                        selected = currentRoute == Screen.Statistics.route,
                                        onClick = {
                                            navController.navigate(Screen.Statistics.route) {
                                                popUpTo(Screen.Home.route)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }

    /**
     * ✅ FCM 토큰 확인 함수
     *
     * FCM이 제대로 작동하는지 확인하는 핵심 코드!
     * 로그를 통해 토큰 생성 성공/실패를 확인할 수 있어요.
     */
    private fun checkFcmToken() {
        Log.d(TAG, "=== FCM 토큰 확인 시작 ===")

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "✅ FCM 토큰 생성 성공!")
                    Log.d(TAG, "🔑 토큰: $token")
                    Log.d(TAG, "📌 Firebase Console에서 이 토큰으로 테스트 알림을 보낼 수 있어요!")
                } else {
                    Log.e(TAG, "❌ FCM 토큰 생성 실패", task.exception)
                    Log.e(TAG, "   에러 메시지: ${task.exception?.message}")

                    // 에러 원인 분석
                    when {
                        task.exception?.message?.contains("SERVICE_NOT_AVAILABLE") == true -> {
                            Log.e(TAG, "   → Google Play Services를 사용할 수 없습니다")
                        }
                        task.exception?.message?.contains("DEVELOPER_ERROR") == true -> {
                            Log.e(TAG, "   → Firebase 프로젝트 설정 문제입니다")
                            Log.e(TAG, "   → google-services.json 파일을 확인하세요")
                        }
                        task.exception?.message?.contains("MISSING_INSTANCEID_SERVICE") == true -> {
                            Log.e(TAG, "   → AndroidManifest.xml 설정을 확인하세요")
                        }
                        else -> {
                            Log.e(TAG, "   → 알 수 없는 에러입니다")
                        }
                    }
                }
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}