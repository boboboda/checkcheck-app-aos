package com.buyoungsil.checkcheck.feature.group.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.coin.domain.model.TransactionType
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupTier
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 그룹 티어 업그레이드 UseCase
 *
 * 그룹장만 업그레이드 가능
 * 코인 차감 후 티어 업그레이드
 */
class UpgradeGroupTierUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val coinRepository: CoinRepository
) {
    companion object {
        private const val TAG = "UpgradeGroupTierUseCase"
    }

    suspend operator fun invoke(
        groupId: String,
        userId: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎯 그룹 티어 업그레이드 시작")
            Log.d(TAG, "  - groupId: $groupId")
            Log.d(TAG, "  - userId: $userId")

            // 1. 그룹 정보 조회
            val group = groupRepository.getGroupById(groupId)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))

            Log.d(TAG, "  - 현재 티어: ${group.tier.displayName}")
            Log.d(TAG, "  - 현재 인원: ${group.currentMemberCount()}/${group.maxMembers}")

            // 2. 권한 확인 (그룹장만 가능)
            if (group.ownerId != userId) {
                Log.e(TAG, "❌ 권한 없음 - 그룹장만 업그레이드 가능")
                return Result.failure(Exception("그룹장만 티어를 업그레이드할 수 있습니다"))
            }

            // 3. 다음 티어 확인
            val nextTier = group.tier.getNextTier()
            if (nextTier == null) {
                Log.e(TAG, "❌ 이미 최고 티어")
                return Result.failure(Exception("이미 최고 티어입니다"))
            }

            // 4. 업그레이드 비용 확인
            val cost = group.tier.upgradeCost
                ?: return Result.failure(Exception("업그레이드할 수 없는 티어입니다"))

            Log.d(TAG, "  - 다음 티어: ${nextTier.displayName}")
            Log.d(TAG, "  - 업그레이드 비용: ${cost}코인")

            // 5. 사용자 코인 확인
            val wallet = coinRepository.getCoinWallet(userId).first()
            if (wallet == null) {
                Log.e(TAG, "❌ 코인 지갑을 찾을 수 없음")
                return Result.failure(Exception("코인 지갑을 찾을 수 없습니다"))
            }

            val totalCoins = wallet.familyCoins + wallet.rewardCoins
            if (totalCoins < cost) {
                Log.e(TAG, "❌ 코인 부족 (보유: ${totalCoins}, 필요: ${cost})")
                return Result.failure(Exception("코인이 부족합니다 (필요: ${cost}코인)"))
            }

            Log.d(TAG, "  - 보유 코인: ${totalCoins}코인")

            // 6. 그룹 업그레이드
            val upgradedGroup = group.copy(
                tier = nextTier,
                maxMembers = nextTier.maxMembers,
                updatedAt = System.currentTimeMillis()
            )
            groupRepository.updateGroup(upgradedGroup)
            Log.d(TAG, "✅ 그룹 티어 업그레이드 완료")

            // 7. 코인 차감 (시스템으로 전송)
            coinRepository.giftCoins(
                fromUserId = userId,
                toUserId = "system",
                amount = cost,
                message = "${group.name} 그룹을 ${nextTier.displayName} 티어로 업그레이드"
            ).onSuccess {
                Log.d(TAG, "✅ 코인 차감 완료")
            }.onFailure { error ->
                Log.e(TAG, "❌ 코인 차감 실패", error)
                // 롤백 필요 (티어 다시 원상복구)
                groupRepository.updateGroup(group)
                return Result.failure(error)
            }

            Log.d(TAG, "🎉 그룹 티어 업그레이드 성공!")
            Log.d(TAG, "  - ${group.tier.displayName} → ${nextTier.displayName}")
            Log.d(TAG, "  - ${group.maxMembers}명 → ${nextTier.maxMembers}명")
            Log.d(TAG, "========================================")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 그룹 티어 업그레이드 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}