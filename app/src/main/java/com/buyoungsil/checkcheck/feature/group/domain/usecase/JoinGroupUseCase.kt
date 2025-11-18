package com.buyoungsil.checkcheck.feature.group.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import javax.inject.Inject

class JoinGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    companion object {
        private const val TAG = "JoinGroupUseCase"
    }

    suspend operator fun invoke(inviteCode: String, userId: String): Result<Group> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "👋 그룹 가입 시작")
            Log.d(TAG, "  - inviteCode: $inviteCode")
            Log.d(TAG, "  - userId: $userId")

            // 1. 초대 코드로 그룹 찾기
            val group = repository.getGroupByInviteCode(inviteCode)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))

            Log.d(TAG, "  - 그룹명: ${group.name}")
            Log.d(TAG, "  - 티어: ${group.tier.displayName}")
            Log.d(TAG, "  - 현재 인원: ${group.currentMemberCount()}/${group.maxMembers}")

            // 2. 이미 가입된 멤버인지 확인
            if (group.memberIds.contains(userId)) {
                Log.w(TAG, "⚠️ 이미 가입된 그룹")
                return Result.failure(Exception("이미 가입된 그룹입니다"))
            }

            // 3. 인원 제한 확인 (티어 기반)
            if (!group.canAddMember()) {
                Log.e(TAG, "❌ 그룹 인원 초과")
                Log.e(TAG, "  - 현재: ${group.currentMemberCount()}명")
                Log.e(TAG, "  - 최대: ${group.maxMembers}명")
                Log.e(TAG, "  - 티어: ${group.tier.displayName}")

                return Result.failure(
                    Exception(
                        "그룹 인원이 가득 찼습니다 (${group.currentMemberCount()}/${group.maxMembers}명)\n" +
                                "그룹장에게 티어 업그레이드를 요청하세요"
                    )
                )
            }

            // 4. 그룹 가입 처리
            repository.joinGroup(group.id, userId)
            Log.d(TAG, "✅ 그룹 가입 성공")
            Log.d(TAG, "========================================")

            Result.success(group)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 그룹 가입 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}