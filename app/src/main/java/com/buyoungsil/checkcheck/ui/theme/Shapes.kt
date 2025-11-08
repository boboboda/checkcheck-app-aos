package com.buyoungsil.checkcheck.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * CheckCheck Shape 시스템
 * 둥글둥글 귀여운 모서리
 */
val Shapes = Shapes(
    // Extra Small - 버튼, 태그
    extraSmall = RoundedCornerShape(8.dp),

    // Small - 작은 카드, 칩
    small = RoundedCornerShape(12.dp),

    // Medium - 일반 카드, 다이얼로그
    medium = RoundedCornerShape(16.dp),

    // Large - 바텀시트, 큰 카드
    large = RoundedCornerShape(20.dp),

    // Extra Large - 특별한 컴포넌트
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * 커스텀 Shape 상수
 */
object CheckShapes {
    val Button = RoundedCornerShape(12.dp)
    val Card = RoundedCornerShape(16.dp)
    val Chip = RoundedCornerShape(20.dp)
    val Dialog = RoundedCornerShape(24.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val FloatingActionButton = RoundedCornerShape(16.dp)

    // 습관 카드 - 약간 큰 둥근 모서리
    val HabitCard = RoundedCornerShape(3.dp)

    // 그룹 카드 - 더 둥근 느낌
    val GroupCard = RoundedCornerShape(24.dp)

    // 프로그레스 바 - 완전 둥근
    val ProgressBar = RoundedCornerShape(100.dp)

    // 아이콘 배경
    val IconBackground = RoundedCornerShape(12.dp)
}