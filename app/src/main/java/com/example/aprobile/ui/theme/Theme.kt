package com.example.aprobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

val MintMintContainer = Color(0xFF8FF5D4)

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintDarkSurfaceVariant,
    onPrimaryContainer = MintMintContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintDarkSurfaceVariant,
    onSecondaryContainer = MintDarkOnSurface,
    background = MintDarkBackground,
    surface = MintDarkSurface,
    surfaceVariant = MintDarkSurfaceVariant,
    onSurface = MintDarkOnSurface,
    onBackground = MintDarkOnSurface,
    onSurfaceVariant = MintDarkOnSurfaceVariant,
    outline = MintDarkOutline,
    tertiaryContainer = MintDarkTertiaryContainer,
    onTertiaryContainer = MintDarkOnTertiaryContainer,
    error = MintDarkError,
    onError = MintDarkOnError
)

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintPrimaryContainer,
    onPrimaryContainer = MintOnPrimaryContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintOnSecondaryContainer,
    background = MintBackground,
    surface = MintSurface,
    surfaceVariant = MintSurfaceVariant,
    onSurface = MintOnSurface,
    onBackground = MintOnSurface,
    onSurfaceVariant = MintOnSurfaceVariant,
    outline = MintOutline,
    tertiaryContainer = MintTertiaryContainer,
    onTertiaryContainer = MintOnTertiaryContainer,
    error = MintError,
    onError = MintOnError
)

@Composable
fun AprobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
