package com.buyoungsil.checkcheck.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 기기 재시작 시 알림 재설정
 */

class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: 저장된 모든 리마인더를 다시 스케줄
            // 현재는 앱 실행 시 자동으로 재설정됨
        }
    }
}