package com.buyoungsil.checkcheck.core.util

/**
 * 아이콘 텍스트를 이모지로 변환하는 유틸리티
 * 기존에 잘못 저장된 텍스트를 이모지로 변환
 */
object IconConverter {

    private val iconMap = mapOf(
        // 물 관련
        "water" to "💧",
        "water_d" to "💧",
        "water_drop" to "💧",

        // 운동 관련
        "direction" to "🏃",
        "directio" to "🏃",
        "run" to "🏃",
        "exercise" to "💪",
        "fitness" to "🏋️",

        // 독서/공부
        "book" to "📚",
        "read" to "📖",
        "study" to "📝",

        // 음식
        "food" to "🍽️",
        "apple" to "🍎",
        "salad" to "🥗",

        // 수면
        "sleep" to "😴",
        "bed" to "🛏️",

        // 명상/요가
        "meditation" to "🧘",
        "yoga" to "🧘‍♀️",

        // 음악
        "music" to "🎵",
        "piano" to "🎹",

        // 그림/예술
        "art" to "🎨",
        "paint" to "🖌️",

        // 기타
        "heart" to "❤️",
        "star" to "⭐",
        "fire" to "🔥",
        "check" to "✅",
        "pin" to "📌",

        // 기본값
        "" to "📌",
        "default" to "📌"
    )

    /**
     * 텍스트 아이콘을 이모지로 변환
     * 이미 이모지인 경우 그대로 반환
     */
    fun convertToEmoji(icon: String): String {
        // 이미 이모지인 경우 (1-4자 정도의 유니코드 이모지)
        if (icon.length <= 4 && icon.any { it.code > 127 }) {
            return icon
        }

        // 텍스트를 소문자로 변환해서 매핑
        val lowerIcon = icon.lowercase().trim()

        // 정확히 일치하는 경우
        iconMap[lowerIcon]?.let { return it }

        // 부분 일치하는 경우 찾기
        iconMap.entries.find { (key, _) ->
            lowerIcon.contains(key) || key.contains(lowerIcon)
        }?.value?.let { return it }

        // 매핑되지 않으면 기본 아이콘
        return "📌"
    }

    /**
     * 습관 제목으로부터 추천 이모지 반환
     */
    fun getEmojiByTitle(title: String): String {
        val lowerTitle = title.lowercase()

        return when {
            lowerTitle.contains("물") || lowerTitle.contains("water") -> "💧"
            lowerTitle.contains("운동") || lowerTitle.contains("exercise") || lowerTitle.contains("헬스") -> "🏃"
            lowerTitle.contains("독서") || lowerTitle.contains("책") || lowerTitle.contains("book") -> "📚"
            lowerTitle.contains("명상") || lowerTitle.contains("meditation") -> "🧘"
            lowerTitle.contains("공부") || lowerTitle.contains("study") -> "📝"
            lowerTitle.contains("식사") || lowerTitle.contains("밥") || lowerTitle.contains("음식") -> "🍽️"
            lowerTitle.contains("수면") || lowerTitle.contains("잠") || lowerTitle.contains("sleep") -> "😴"
            lowerTitle.contains("음악") || lowerTitle.contains("music") -> "🎵"
            lowerTitle.contains("그림") || lowerTitle.contains("art") -> "🎨"
            lowerTitle.contains("요가") || lowerTitle.contains("yoga") -> "🧘‍♀️"
            else -> "📌"
        }
    }
}