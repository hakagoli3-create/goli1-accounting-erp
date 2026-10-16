package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = SurfaceDark,
    primaryContainer = CardBackgroundDark,
    onPrimaryContainer = PrimaryDark,
    secondary = PersianGold,
    onSecondary = SurfaceDark,
    secondaryContainer = GoldOnContainer,
    onSecondaryContainer = GoldContainer,
    tertiary = DeepTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = CardBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = PersianGold,
    onSecondary = EmeraldOnPrimary,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldOnContainer,
    tertiary = DeepTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = CardBackgroundLight
)

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
