package com.buyoungsil.checkcheck.core.notification.domain.model

import java.time.LocalTime

/**
 * 알림 리마인더 모델
 */
data class Reminder(
    val id: String,
    val habitId: String,
    val habitTitle: String,
    val time: LocalTime,
    val enabled: Boolean = true,
    val days: Set<DayOfWeek> = DayOfWeek.values().toSet(), // 매일 기본
    val message: String? = null
)

enum class DayOfWeek(val displayName: String) {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    fun toJavaDayOfWeek(): java.time.DayOfWeek {
        return when (this) {
            MONDAY -> java.time.DayOfWeek.MONDAY
            TUESDAY -> java.time.DayOfWeek.TUESDAY
            WEDNESDAY -> java.time.DayOfWeek.WEDNESDAY
            THURSDAY -> java.time.DayOfWeek.THURSDAY
            FRIDAY -> java.time.DayOfWeek.FRIDAY
            SATURDAY -> java.time.DayOfWeek.SATURDAY
            SUNDAY -> java.time.DayOfWeek.SUNDAY
        }
    }
}