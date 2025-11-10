package com.buyoungsil.checkcheck.core.data.firebase

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // 사용자가 익명 로그인 상태인지 확인
    val isAnonymous: Boolean
        get() = auth.currentUser?.isAnonymous == true

    // 사용자 상태 Flow
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        Log.d(TAG, "=== authStateFlow 시작 ===")

        // ✨✨✨ 핵심! 즉시 현재 사용자 emit (무한 로딩 방지)
        val initialUser = auth.currentUser
        Log.d(TAG, "초기 사용자: ${initialUser?.uid}")
        trySend(initialUser)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            Log.d(TAG, "Auth 상태 변경: ${auth.currentUser?.uid}")
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            Log.d(TAG, "authStateFlow 종료")
            auth.removeAuthStateListener(listener)
        }
    }

    // 익명 로그인
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "익명 로그인 시작")
            val result = auth.signInAnonymously().await()
            Log.d(TAG, "✅ 익명 로그인 성공: uid=${result.user?.uid}")
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 익명 로그인 실패", e)
            Result.failure(e)
        }
    }

    // 이메일 로그인
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 이메일 회원가입
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✨ 익명 계정을 이메일 계정으로 연동
    suspend fun linkAnonymousWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser
            if (user == null || !user.isAnonymous) {
                return Result.failure(Exception("익명 사용자가 아닙니다"))
            }

            val credential = EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 로그아웃
    fun signOut() {
        auth.signOut()
    }
}