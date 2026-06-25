package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBg,
    surface = CyberSurface,
    onPrimary = CyberBg,
    onSecondary = CyberTextPrimary,
    onTertiary = CyberBg,
    onBackground = CyberTextPrimary,
    onSurface = CyberTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force sci-fi dark theme for JARVIS
  dynamicColor: Boolean = false, // Force custom brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
