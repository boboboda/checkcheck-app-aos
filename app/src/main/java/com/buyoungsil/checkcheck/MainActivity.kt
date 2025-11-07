package com.buyoungsil.checkcheck

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.domain.usecase.InitializeUserUseCase
import com.buyoungsil.checkcheck.core.domain.usecase.UpdateFcmTokenUseCase
import com.buyoungsil.checkcheck.core.ui.navigation.NavGraph
import com.buyoungsil.checkcheck.core.ui.navigation.Screen
import com.buyoungsil.checkcheck.ui.theme.CheckcheckTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: FirebaseAuthManager

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var initializeUserUseCase: InitializeUserUseCase

    // ✅ 알림 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ 알림 권한 허용됨")
        } else {
            Log.d(TAG, "❌ 알림 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 0. Firebase 익명 로그인 (가장 먼저!)
        lifecycleScope.launch {
            if (authManager.currentUser == null) {
                Log.d(TAG, "⏳ Firebase 익명 로그인 시도...")
                val result = authManager.signInAnonymously()
                result.onSuccess { user ->
                    Log.d(TAG, "✅ Firebase 익명 로그인 성공")
                    Log.d(TAG, "   User ID: ${user.uid}")

                    // ✅ User 문서 초기화 (Firestore)
                    initializeUserUseCase(user.uid)

                    // FCM 토큰 저장
                    checkAndSaveFcmToken()
                }.onFailure { error ->
                    Log.e(TAG, "❌ Firebase 로그인 실패: ${error.message}")
                }
            } else {
                Log.d(TAG, "✅ 이미 로그인됨: ${authManager.currentUser?.uid}")

                // ✅ User 문서 확인/초기화
                authManager.currentUserId?.let { initializeUserUseCase(it) }

                // FCM 토큰 저장
                checkAndSaveFcmToken()
            }
        }

        // ✅ 1. 알림 권한 요청
        requestNotificationPermission()

        setContent {
            CheckcheckTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // ✅ authStateFlow() 사용
                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (authState == null) {
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
                                    )
                                ) {
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
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * ✅ 알림 권한 요청 (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ 알림 권한 이미 허용됨")
                }
                else -> {
                    Log.d(TAG, "⏳ 알림 권한 요청 중...")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "✅ Android 12 이하 - 알림 권한 자동 허용")
        }
    }

    /**
     * ✅ FCM 토큰 확인 및 Firestore 저장
     */
    private fun checkAndSaveFcmToken() {
        val userId = authManager.currentUserId
        if (userId == null) {
            Log.w(TAG, "⚠️ 로그인 안 된 상태 - FCM 토큰 저장 건너뜀")
            return
        }

        Log.d(TAG, "=== FCM 토큰 확인 시작 ===")

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "✅ FCM 토큰 생성 성공!")
                    Log.d(TAG, "🔑 토큰: $token")

                    // ✅ Firestore에 저장
                    lifecycleScope.launch {
                        try {
                            updateFcmTokenUseCase(userId, token)
                            Log.d(TAG, "✅ FCM 토큰 Firestore 저장 완료")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ FCM 토큰 저장 실패", e)
                        }
                    }
                } else {
                    Log.e(TAG, "❌ FCM 토큰 생성 실패", task.exception)
                    Log.e(TAG, "   에러 메시지: ${task.exception?.message}")
                }
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}