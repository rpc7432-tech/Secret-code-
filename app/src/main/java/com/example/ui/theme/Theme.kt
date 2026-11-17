package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SecretChatExtendedColors(
  val headerBackground: Color,
  val headerAccent: Color,
  val onHeader: Color,
  val onHeaderMuted: Color,
  val inputBackground: Color,
  val outputBackground: Color,
  val cardBorder: Color,
  val decryptButtonContainer: Color,
  val onDecryptButton: Color,
  val infoContainer: Color,
  val infoBorder: Color,
  val onInfoContainer: Color,
  val errorBannerContainer: Color,
  val errorBannerBorder: Color,
  val onErrorBanner: Color,
  val copiedContainer: Color,
  val copiedBorder: Color,
  val onCopiedContainer: Color,
)

private val LightExtendedColors =
  SecretChatExtendedColors(
    headerBackground = AcademicBlue900,
    headerAccent = AcademicAmber400,
    onHeader = Color.White,
    onHeaderMuted = AcademicBlue100,
    inputBackground = Slate50,
    outputBackground = Slate100,
    cardBorder = Slate300,
    decryptButtonContainer = AcademicEmerald700,
    onDecryptButton = Color.White,
    infoContainer = AcademicBlue50,
    infoBorder = AcademicBlue100,
    onInfoContainer = AcademicBlue900,
    errorBannerContainer = Rose50,
    errorBannerBorder = Rose300,
    onErrorBanner = Rose800,
    copiedContainer = AcademicEmerald50,
    copiedBorder = AcademicEmerald300,
    onCopiedContainer = AcademicEmerald800,
  )

private val DarkExtendedColors =
  SecretChatExtendedColors(
    headerBackground = DarkAcademicBlue950,
    headerAccent = AcademicAmber400,
    onHeader = Slate50,
    onHeaderMuted = AcademicBlue100,
    inputBackground = Slate900,
    outputBackground = Color(0xFF0B1120),
    cardBorder = Slate700,
    decryptButtonContainer = DarkEmerald500,
    onDecryptButton = Color(0xFF022C22),
    infoContainer = DarkAcademicBlue950,
    infoBorder = AcademicBlue800,
    onInfoContainer = AcademicBlue100,
    errorBannerContainer = DarkRose950,
    errorBannerBorder = DarkRose800,
    onErrorBanner = DarkRose300,
    copiedContainer = DarkEmerald950,
    copiedBorder = AcademicEmerald700,
    onCopiedContainer = DarkEmerald200,
  )

val LocalSecretChatColors = staticCompositionLocalOf { LightExtendedColors }

private val LightColorScheme =
  lightColorScheme(
    primary = AcademicBlue800,
    onPrimary = Color.White,
    primaryContainer = AcademicBlue50,
    onPrimaryContainer = AcademicBlue900,
    secondary = AcademicEmerald700,
    onSecondary = Color.White,
    secondaryContainer = AcademicEmerald50,
    onSecondaryContainer = AcademicEmerald800,
    tertiary = AcademicAmber400,
    onTertiary = Slate900,
    background = Slate100,
    onBackground = Slate800,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Rose800,
    onError = Color.White,
    errorContainer = Rose50,
    onErrorContainer = Rose800,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkAcademicBlue400,
    onPrimary = DarkAcademicBlue950,
    primaryContainer = DarkAcademicBlue950,
    onPrimaryContainer = AcademicBlue100,
    secondary = DarkEmerald500,
    onSecondary = DarkEmerald950,
    secondaryContainer = DarkEmerald950,
    onSecondaryContainer = DarkEmerald200,
    tertiary = AcademicAmber400,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate900,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700,
    error = DarkRose300,
    onError = DarkRose950,
    errorContainer = DarkRose950,
    onErrorContainer = DarkRose300,
  )

object SecretChatThemeTokens {
  val extendedColors: SecretChatExtendedColors
    @Composable get() = LocalSecretChatColors.current
}

@Composable
fun SecretChatTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

  CompositionLocalProvider(LocalSecretChatColors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = SecretChatTypography,
      content = content,
    )
  }
}
