package com.buyoungsil.checkcheck.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * CheckCheck Light Theme
 * 진짜 MZ감성 - 엣지투엣지, 상태바 투명
 */
private val LightColorScheme = lightColorScheme(
    primary = CheckPrimary,
    onPrimary = Color.White,
    primaryContainer = CheckPrimaryLight,
    onPrimaryContainer = CheckPrimaryDark,

    secondary = CheckSecondary,
    onSecondary = Color.White,
    secondaryContainer = CheckSecondaryLight,
    onSecondaryContainer = CheckSecondaryDark,

    tertiary = CheckPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE9DBFF),
    onTertiaryContainer = Color(0xFF7C4DFF),

    error = CheckError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFFBA1A1A),

    background = CheckBgPrimary,
    onBackground = CheckGray900,

    surface = CheckBgPrimary,
    onSurface = CheckGray900,
    surfaceVariant = CheckBgTertiary,
    onSurfaceVariant = CheckGray700,

    outline = CheckGray300,
    outlineVariant = CheckGray200,

    surfaceTint = CheckPrimary,

    inverseSurface = CheckGray800,
    inverseOnSurface = CheckGray50,
    inversePrimary = CheckPrimaryLight,

    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * CheckCheck Dark Theme
 */
private val DarkColorScheme = darkColorScheme(
    primary = CheckPrimaryLight,
    onPrimary = CheckPrimaryDark,
    primaryContainer = CheckPrimaryDark,
    onPrimaryContainer = CheckPrimaryLight,

    secondary = CheckSecondaryLight,
    onSecondary = CheckSecondaryDark,
    secondaryContainer = CheckSecondaryDark,
    onSecondaryContainer = CheckSecondaryLight,

    tertiary = Color(0xFFD4B3FF),
    onTertiary = Color(0xFF5E35B1),
    tertiaryContainer = Color(0xFF7C4DFF),
    onTertiaryContainer = Color(0xFFE9DBFF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = CheckBgPrimaryDark,
    onBackground = CheckGray100,

    surface = CheckBgPrimaryDark,
    onSurface = CheckGray100,
    surfaceVariant = CheckBgTertiaryDark,
    onSurfaceVariant = CheckGray300,

    outline = CheckGray600,
    outlineVariant = CheckGray700,

    surfaceTint = CheckPrimaryLight,

    inverseSurface = CheckGray100,
    inverseOnSurface = CheckGray800,
    inversePrimary = CheckPrimary,

    scrim = Color.Black.copy(alpha = 0.32f)
)

@Composable
fun CheckCheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // ✨ 상태바 완전 투명 처리
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // ✨ 엣지 투 엣지 활성화
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // ✨ 상태바 아이콘 색상 (라이트 모드에서는 검은색)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}