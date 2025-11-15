package com.buyoungsil.checkcheck.feature.task.domain.model

/**
 * 태스크 승인 상태
 */
enum class ApprovalStatus(val displayName: String) {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨")
}