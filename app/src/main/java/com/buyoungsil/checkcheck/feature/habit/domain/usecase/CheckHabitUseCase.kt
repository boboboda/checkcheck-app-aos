package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 습관 체크 UseCase
 *
 * ✅ 토글 방식에서 **체크만 가능** 방식으로 변경
 *
 * ## 변경 이유
 * 1. 실수로 해제 시 기록 손실 방지
 * 2. 코인 어뷰징 차단 (체크 → 해제 → 재체크)
 * 3. 마일스톤 데이터 정합성 유지
 *
 * ## 동작 방식
 * - 오늘 체크 안 함 → 체크 추가 ✅
 * - 오늘 이미 체크 → 아무 동작 안 함 (Success 반환)
 *
 * @since 2025-01-15 (토글 방식 제거)
 */
class CheckHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    companion object {
        private const val TAG = "CheckHabitUseCase"
    }

    /**
     * 습관 체크 (체크만 가능, 해제 불가)
     *
     * @param habitId 습관 ID
     * @param userId 사용자 ID
     * @param date 체크할 날짜 (기본: 오늘)
     * @return Result<Boolean> - true: 체크 추가됨, false: 이미 체크되어 있음
     */
    suspend operator fun invoke(
        habitId: String,
        userId: String,
        date: LocalDate = LocalDate.now()
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "=== 습관 체크 시작 ===")
            Log.d(TAG, "habitId: $habitId")
            Log.d(TAG, "date: $date")

            // 1. 이미 체크했는지 확인
            val existingCheck = repository.getCheckByDate(habitId, date)

            if (existingCheck != null) {
                Log.d(TAG, "⚠️ 이미 체크되어 있음 - 동작 없음")
                return Result.success(false)  // 이미 체크됨
            }

            // 2. 새 체크 추가
            Log.d(TAG, "✅ 새 체크 추가")
            repository.toggleHabitCheck(habitId, userId, date)

            Log.d(TAG, "🎉 습관 체크 완료")
            Result.success(true)  // 체크 추가됨

        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 체크 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 특정 날짜의 체크 상태 확인
     *
     * @param habitId 습관 ID
     * @param date 확인할 날짜
     * @return true: 체크됨, false: 체크 안 됨
     */
    suspend fun isChecked(
        habitId: String,
        date: LocalDate = LocalDate.now()
    ): Boolean {
        return try {
            val check = repository.getCheckByDate(habitId, date)
            check != null && check.completed
        } catch (e: Exception) {
            Log.e(TAG, "체크 상태 확인 실패", e)
            false
        }
    }
}