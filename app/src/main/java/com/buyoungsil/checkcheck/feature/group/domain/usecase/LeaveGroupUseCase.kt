package com.buyoungsil.checkcheck.feature.group.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import javax.inject.Inject

class LeaveGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    companion object {
        private const val TAG = "LeaveGroupUseCase"
    }

    suspend operator fun invoke(groupId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "=== 그룹 나가기 시작 (groupId=$groupId, userId=$userId) ===")

            val group = repository.getGroupById(groupId)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))

            Log.d(TAG, "그룹 정보: ${group.name}, ownerId=${group.ownerId}, memberIds=${group.memberIds}")

            // ✅ 그룹장이 나가는 경우
            if (group.ownerId == userId) {
                Log.d(TAG, "그룹장이 나가려고 함")

                // 다른 멤버가 있으면 첫 번째 멤버에게 그룹장 이양
                val otherMembers = group.memberIds.filter { it != userId }

                if (otherMembers.isNotEmpty()) {
                    val newOwnerId = otherMembers.first()
                    Log.d(TAG, "그룹장 이양: $userId → $newOwnerId")

                    // 그룹장 변경
                    val updatedGroup = group.copy(ownerId = newOwnerId)
                    repository.updateGroup(updatedGroup)
                    Log.d(TAG, "✅ 그룹장 변경 완료")
                } else {
                    // 마지막 멤버면 그룹 삭제
                    Log.d(TAG, "마지막 멤버 → 그룹 삭제")
                    repository.deleteGroup(groupId)
                    Log.d(TAG, "✅ 그룹 삭제 완료")
                    return Result.success(Unit)
                }
            }

            // 멤버 목록에서 제거
            repository.leaveGroup(groupId, userId)
            Log.d(TAG, "✅ 그룹 나가기 완료")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 그룹 나가기 실패", e)
            Result.failure(e)
        }
    }
}