package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = PersianGoldLight,
    onPrimary = CharcoalDark,
    primaryContainer = CharcoalCard,
    onPrimaryContainer = PersianGoldLight,
    secondary = PersianGold,
    onSecondary = CharcoalDark,
    secondaryContainer = CharcoalSecondary,
    onSecondaryContainer = PersianGoldContainer,
    tertiary = SlateBlueAccent,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CharcoalPrimary,
    onPrimary = Color.White,
    primaryContainer = WarmBeigeCard,
    onPrimaryContainer = CharcoalPrimary,
    secondary = PersianGold,
    onSecondary = Color.White,
    secondaryContainer = PersianGoldContainer,
    onSecondaryContainer = PersianGoldOnContainer,
    tertiary = SlateBlueAccent,
    background = WarmCreamBackground,
    onBackground = WarmTextDark,
    surface = CreamSurface,
    onSurface = WarmTextDark,
    surfaceVariant = WarmBeigeCard,
    onSurfaceVariant = WarmTextMuted,
    outline = BeigeBorder
)

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(false) }

@Composable
fun GoliAccountingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GoliAccountingTheme(darkTheme = darkTheme, content = content)
}
