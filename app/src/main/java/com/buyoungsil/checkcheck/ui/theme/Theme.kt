package com.buyoungsil.checkcheck.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🧡 Light Orange Theme
private val LightOrangeColorScheme = lightColorScheme(
    // Primary Colors
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeLight,
    onPrimaryContainer = OrangeDark,

    // Secondary Colors
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = PeachAccent,
    onSecondaryContainer = OrangeDark,

    // Tertiary Colors
    tertiary = OrangeTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SunsetAccent,
    onTertiaryContainer = OrangeDark,

    // Error Colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Background Colors
    background = OrangeBackground,
    onBackground = TextPrimaryLight,

    // Surface Colors
    surface = OrangeSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = OrangeSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,

    // Outline
    outline = DividerLight,
    outlineVariant = Color(0xFFE0E0E0),

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = DarkOrangePrimary,
)

// 🌙 Dark Orange Theme
private val DarkOrangeColorScheme = darkColorScheme(
    // Primary Colors
    primary = DarkOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeDark,
    onPrimaryContainer = OrangeLight,

    // Secondary Colors
    secondary = DarkOrangeSecondary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = Color(0xFF4A3527),
    onSecondaryContainer = PeachAccent,

    // Tertiary Colors
    tertiary = OrangeTertiary,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = Color(0xFF5A3D2F),
    onTertiaryContainer = SunsetAccent,

    // Error Colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background Colors
    background = DarkBackground,
    onBackground = TextPrimaryDark,

    // Surface Colors
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    // Outline
    outline = DividerDark,
    outlineVariant = Color(0xFF3D3D3D),

    // Other
    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = OrangeSurface,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = OrangePrimary,
)

@Composable
fun CheckCheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 오렌지 테마 우선, Dynamic Color 비활성화
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkOrangeColorScheme
        else -> LightOrangeColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 상태바 색상 설정
            window.statusBarColor = if (darkTheme) {
                DarkBackground.toArgb()
            } else {
                OrangeBackground.toArgb()
            }

            // 상태바 아이콘 색상 설정
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            // 네비게이션바 색상 설정
            window.navigationBarColor = if (darkTheme) {
                DarkSurface.toArgb()
            } else {
                OrangeSurface.toArgb()
            }

            // 네비게이션바 아이콘 색상 설정
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CheckShapes,
        content = content
    )
}

// ===== 🎨 Theme Extensions =====

/**
 * 그룹 타입별 색상 가져오기
 */
fun getGroupTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "family", "가족" -> FamilyOrange
        "couple", "연인" -> CoupleRose
        "study", "스터디" -> StudyBlue
        "exercise", "운동" -> ExerciseGreen
        "project", "프로젝트" -> ProjectPurple
        else -> CustomYellow
    }
}

/**
 * 우선순위별 색상 가져오기
 */
fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "urgent", "긴급" -> PriorityUrgent
        "high", "높음" -> PriorityHigh
        "medium", "보통" -> PriorityMedium
        else -> PriorityLow
    }
}

/**
 * 스트릭별 색상 가져오기
 */
fun getStreakColor(days: Int): Color {
    return when {
        days >= 30 -> StreakDiamond
        days >= 7 -> StreakGold
        else -> StreakFire
    }
}

/**
 * 달성률별 색상 가져오기
 */
fun getCompletionColor(percentage: Float): Color {
    return when {
        percentage >= 80f -> SuccessOrange
        percentage >= 50f -> WarningAmber
        else -> ErrorRed
    }
}