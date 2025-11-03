package com.buyoungsil.checkcheck.core.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CheckCheckMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 새 FCM 토큰: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)  // ← 수정
        Log.d(TAG, "📨 FCM 메시지 수신: ${remoteMessage.notification?.title}")
    }

    companion object {
        private const val TAG = "CheckCheckFCM"
    }
}