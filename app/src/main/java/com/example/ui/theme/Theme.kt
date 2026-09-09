package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PinkSecondary,
    secondary = PinkTertiary,
    tertiary = CelebrationPink,
    background = Color(0xFF1F1116),
    surface = Color(0xFF2B1920),
    surfaceVariant = Color(0xFF3B222C),
    primaryContainer = Color(0xFF5E1730),
    onPrimaryContainer = Color(0xFFFFD9E2),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = CelebrationPink,
    background = CreamBackground,
    surface = CreamSurface,
    surfaceVariant = CreamSurfaceVariant,
    primaryContainer = PinkContainer,
    onPrimaryContainer = PinkOnContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF2B1920),
    onSurface = Color(0xFF2B1920),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep pink identity intact by default rather than overriding with wallpaper colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

