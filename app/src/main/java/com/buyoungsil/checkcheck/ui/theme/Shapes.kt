package com.buyoungsil.checkcheck.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 🧡 CheckCheck 오렌지 테마의 Shape 정의
 * 따뜻하고 부드러운 느낌의 둥근 모서리
 */
val CheckShapes = Shapes(
    // 작은 컴포넌트 (버튼, 칩 등)
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),

    // 중간 컴포넌트 (카드, 다이얼로그 등)
    medium = RoundedCornerShape(16.dp),

    // 큰 컴포넌트 (바텀 시트, 큰 카드 등)
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * 컴포넌트별 커스텀 Shape
 */
object ComponentShapes {
    // 🎴 카드 Shape
    val HabitCard = RoundedCornerShape(20.dp)           // 습관 카드
    val GroupCard = RoundedCornerShape(24.dp)           // 그룹 카드 (더 둥글게)
    val TaskCard = RoundedCornerShape(16.dp)            // 할일 카드
    val StatCard = RoundedCornerShape(20.dp)            // 통계 카드

    // 🔘 버튼 Shape
    val PrimaryButton = RoundedCornerShape(16.dp)       // 주요 버튼
    val SecondaryButton = RoundedCornerShape(12.dp)     // 보조 버튼
    val IconButton = RoundedCornerShape(12.dp)          // 아이콘 버튼
    val FloatingButton = RoundedCornerShape(28.dp)      // FAB

    // 🏷️ 칩 & 뱃지 Shape
    val Chip = RoundedCornerShape(20.dp)                // 칩 (완전 둥근)
    val Badge = RoundedCornerShape(12.dp)               // 뱃지
    val Tag = RoundedCornerShape(8.dp)                  // 태그

    // 📋 입력 필드 Shape
    val TextField = RoundedCornerShape(16.dp)           // 텍스트 필드
    val SearchBar = RoundedCornerShape(24.dp)           // 검색창 (더 둥글게)

    // 🖼️ 아이콘 배경 Shape
    val IconBackground = RoundedCornerShape(16.dp)      // 아이콘 원형 배경
    val AvatarSmall = RoundedCornerShape(12.dp)         // 작은 아바타
    val AvatarMedium = RoundedCornerShape(16.dp)        // 중간 아바타
    val AvatarLarge = RoundedCornerShape(20.dp)         // 큰 아바타

    // 📱 다이얼로그 Shape
    val Dialog = RoundedCornerShape(28.dp)              // 다이얼로그
    val BottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )                                                    // 바텀 시트

    // 📊 차트 Shape
    val ChartBar = RoundedCornerShape(8.dp)             // 차트 바
    val ChartCard = RoundedCornerShape(20.dp)           // 차트 카드

    // 🎨 프로그레스 Shape
    val ProgressBar = RoundedCornerShape(12.dp)         // 프로그레스 바
    val ProgressTrack = RoundedCornerShape(12.dp)       // 프로그레스 트랙

    // 📢 알림 Shape
    val NotificationCard = RoundedCornerShape(16.dp)    // 알림 카드
    val ToastMessage = RoundedCornerShape(12.dp)        // 토스트 메시지

    // 🏆 특수 Shape
    val AchievementBadge = RoundedCornerShape(20.dp)    // 업적 뱃지
    val StreakFlame = RoundedCornerShape(16.dp)         // 스트릭 불꽃 배경
}

/**
 * 애니메이션용 Shape 변형
 */
object AnimatedShapes {
    // 체크 시 Shape 변화
    val CheckedShape = RoundedCornerShape(24.dp)        // 체크됨 (더 둥글게)
    val UncheckedShape = RoundedCornerShape(16.dp)      // 체크 안됨

    // 활성/비활성 Shape
    val ActiveShape = RoundedCornerShape(20.dp)         // 활성화
    val InactiveShape = RoundedCornerShape(16.dp)       // 비활성화
}